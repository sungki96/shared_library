import groovy.util.*
import groovy.json.*
import groovy.transform.Field

properties([pipelineTriggers([githubPush()])])

def call(Map config) {

def configurationYMLFilePath="";
def configurationYML="";
def pipelineDirectory = "";
def tf_path = TF_PATH
def exec_node = (config.exec_node) ? config.exec_node : 'master'
    //agent any
    node(exec_node) {
        pipelineDirectory = "${env.WORKSPACE}/home/mgmtbld/demo_01"
        stage('GIT CHECKOUT') {
            
                dir('/home/mgmtbld/demo_01') {
                    println('SCM Checkout started')
                    checkout changelog: false, poll: true, scm: [$class: 'GitSCM', branches: [[name: 'main']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'subhashree', url: 'https://github.com/hiteshcloudwork/platformui.git']]]
                    println('SCM Checkout Completed')
                    sh script: '''
                    ls -la
                    pwd
                    cd yml
                    ls -la
                    '''
                    //Map ymlconfig = [configurationYMLFilePath : "/home/mgmtbld/decoder/yml"]
                    //loadConfigurationYML(configTool: ymlconfig)    
                }
                  
            
        }
        stage('INFRA CREATION') {
        
                //tf_path = configurationYML.terraform.tfPath
                println('TF_PATH'+tf_path)
            dir(tf_path) {
                    println('TARGET INFRA CREATION STARTED')
                    sh script: ''' 
                        pwd
                        terraform fmt | sed -i '1d' main.tf
                        terraform init -upgrade=true
                        terraform plan
                    '''
                    println('TARGET INFRA CREATED')
                }
            
        }
    
    }
}

def loadConfigurationYML (Map config) {
    Boolean returnVal = true;
    returnVal = dir('/home/mgmtbld/demo_01/yml') {
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

//terraform apply -auto-approve
