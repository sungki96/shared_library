/*#import groovy.util.*
import groovy.json.*
import groovy.transform.Field*/

properties([pipelineTriggers([githubPush()])])
@Field def configurationYMLFilePath="";
@Field def configurationYML="";
def pipelineDirectory = "";
def tf_path = "";

def call(Map config) {
    agent any
    stages {
        pipelineDirectory = "${env.WORKSPACE}"
        stage('GIT CHECKOUT') {
            steps {
                dir('/home/mgmtbld/decoder') {
                    println('SCM Checkout started')
                    checkout changelog: false, poll: true, scm: [$class: 'GitSCM', branches: [[name: 'main']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'subhashree', url: 'https://github.com/hiteshcloudwork/platformui.git']]]
                    println('SCM Checkout Completed')
                    Map ymlconfig = [configurationYMLFilePath : "${pipelineDirectory}/yml"]
                    loadConfigurationYML(configTool: ymlconfig)    
                }
                  
            }
        }
        stage('INFRA CREATION') {
            steps {
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
}

def loadConfigurationYML (Map config) {
    Boolean returnVal = true;
    returnVal = dir(env.pipelineDirectory+"/yml") {
        def validationFailed = false;
        def fileName = "app.yml";
        configurationYMLFilePath = config.configTool.configurationYMLFilePath
        dir(configurationYMLFilePath) {
            if(fileExists(fileName)) {
                configurationYML = readYaml file: fileName;
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
