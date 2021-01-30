import groovy.util.*
import groovy.json.*
import groovy.transform.Field

properties([pipelineTriggers([githubPush()])])

def call(Map config) {

def configurationYMLFilePath="";
def configurationYML="";
def pipelineDirectory = "";
def tf_path = "";
def exec_node = (config.exec_node) ? config.exec_node : 'master'
    //agent any
    node(exec_node) {
        pipelineDirectory = "${env.WORKSPACE}/home/mgmtbld/decoder"
        stage('GIT CHECKOUT') {
            
                dir('/home/mgmtbld/decoder') {
                    println('SCM Checkout started')
                    checkout changelog: false, poll: true, scm: [$class: 'GitSCM', branches: [[name: 'main']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'subhashree', url: 'https://github.com/hiteshcloudwork/platformui.git']]]
                    println('SCM Checkout Completed')
                    sh script: '''
                    ls -la
                    pwd
                    cd yml
                    ls -la
                    '''
                    Map ymlconfig = [configurationYMLFilePath : "/home/mgmtbld/decoder/yml"]
                    loadConfigurationYML(configTool: ymlconfig)    
                }
                  
            
        }
        stage('INFRA CREATION') {
        
                tf_path = configurationYML.terraform.tfPath
                dir('${tf_path}') {
                    println('TARGET INFRA CREATION STARTED')
                    sh script: ''' 
                        pwd
                        terraform fmt
                        set -i '1d' main.tf
                        terraform init
                        terraform plan
                        terraform apply -auto-approve
                    '''
                    println('TARGET INFRA CREATED')
                }
            
        }
    
    }
}

def loadConfigurationYML (Map config) {
    Boolean returnVal = true;
    returnVal = dir('/home/mgmtbld/decoder/yml') {
        def validationFailed = false;
        def fileName = "app.yml"
        configurationYMLFilePath = config.configTool.configurationYMLFilePath
        dir(configurationYMLFilePath) {
            if(fileExists(fileName)) {
                configurationYML = readYaml file: "app.yml"
                println("terraform path:"+configurationYML.terraform.tfpath)
            } else {
                println("Configuration YML file not found. File name: ${fileName}. ")
                validationFailed = true;
            }
        }
        if (validationFailed) {
            println("Application Configuration Loading - FAILED");
            return false;
        } else {
            println("Application Configuration Loading - Completed");
        }
        return true;
    }
    return returnVal;
}
