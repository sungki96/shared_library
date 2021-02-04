import groovy.util.*
import groovy.json.*
import groovy.transform.Field
//import com.cloudbees.hudson.plugins.modeling.impl.auxiliary.AuxInstance
import org.jenkinsci.plugins.workflow.support.steps.input.ApproverAction
import org.jenkinsci.plugins.scriptsecurity.scripts.*
import com.demo.common.ColorStep

properties([pipelineTriggers([githubPush()])])

def call(Map config) {

def configurationYMLFilePath="";
def configurationYML="";
def pipelineDirectory = "";
def tf_path = TF_PATH
//def tf_path = ""
def exec_node = (config.exec_node) ? config.exec_node : 'master'
ColorStep color = new ColorStep()
    //agent any
    node(exec_node) {
        pipelineDirectory = "${env.WORKSPACE}/home/mgmtbld/demo_03"
        stage('GIT CHECKOUT') {
            
                dir('/home/mgmtbld/demo_03') {
                    color.blue('SCM Checkout started')
                    checkout changelog: false, poll: true, scm: [$class: 'GitSCM', branches: [[name: 'main']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'subhashree', url: 'https://github.com/hiteshcloudwork/platformui.git']]]
                    color.green('SCM Checkout Completed')
                    sh script: '''
                    ls -la
                    pwd
                    cd yml
                    ls -la
                    '''
                    //Map ymlconfig = [configurationYMLFilePath : "/home/mgmtbld/demo_03/yml"]
                    //loadConfigurationYML(configTool: ymlconfig)    
                }
                  
            
        }
        /*stage('INFRA CREATION') {
        
                //tf_path = configurationYML.terraform.tfPath
                println('TF_PATH'+tf_path)
            dir(tf_path) {
                    color.blue('TARGET INFRA CREATION STARTED')
                    sh script: ''' 
                        pwd
                        terraform fmt | sed -i '1d' main.tf
                        terraform init -upgrade=true
                        terraform plan
                        terraform apply -auto-approve
                    '''
                    color.green('TARGET INFRA CREATED')
                }
            
        }*/
		stage('VALIDATION') {
			dir(tf_path) {
			color.blue('VALIDATION STARTED')
			def ipaddress = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_compute_instance") | .values.network_interface[].network_ip'""", returnStdout: true).toString().trim()
			def instance_name = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_compute_instance") | .values.name'""", returnStdout: true).toString().trim()
			def dns_name = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_dns_managed_zone") | .values.dns_name'""", returnStdout: true).toString().trim()
			def dns_record = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_dns_record_set") | .values.name'""", returnStdout: true).toString().trim()
			def network_name = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_compute_network") | .values.name'""", returnStdout: true).toString().trim()
			def subnetwork_name = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_compute_subnetwork") | .values.name'""", returnStdout: true).toString().trim()
			def serviceAccount = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_service_account") | .values.display_name'""", returnStdout: true).toString().trim()
			def fireWall = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_compute_firewall") | .values.name'""", returnStdout: true).toString().trim()
			def imageName = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_compute_image") | .values.family'""", returnStdout: true).toString().trim()
			def diskName = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_compute_disk") | .values.name'""", returnStdout: true).toString().trim()
			def templateName = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_compute_instance_template") | .name'""", returnStdout: true).toString().trim()
			def sqlInstance = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_sql_database_instance") | .name'""", returnStdout: true).toString().trim()
			def dbName = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_sql_database") | .values.name'""", returnStdout: true).toString().trim()
			def sqlUser = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_sql_user") | .values.name'""", returnStdout: true).toString().trim()
			def forwardingRule = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_compute_forwarding_rule") | .values.name'""", returnStdout: true).toString().trim()
			def backendService = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_compute_region_backend_service") | .values.name'""", returnStdout: true).toString().trim()
			def healthCheck = sh(script: """terraform show -json | jq -r '.values.root_module.resource[] | select (.type=="google_compute_region_health_check") | .values.name'""", returnStdout: true).toString().trim()
			color.blue("INSTANCE IP ADDRESS: ${ipaddress} \nINSTANCE NAME: ${instance_name} \nDNS NAME: ${dns_name} \DNS RECORD SET: ${dns_record} \nNETWORK NAME: ${network_name} \nSUBNETWORK NAME: ${subnetwork_name}
			\nSERVICE ACCOUNT: ${serviceAccount} \nFIREWALL NAME: ${fireWall} \nIMAGE NAME: ${imageName} \nDISK NAME: ${diskName} \nTEMPLATE NAME: ${templateName} \nSQL INSTANCE: ${sqlInstance} 
			\nDB NAME: ${dbName} \nSQL USER: ${sqlUser} \nFORWARDING RULE: ${forwardingRule} \nBACKEND SERVICE NAME: ${backendService} \nHEALTH CHECK NAME: ${healthCheck}")
			color.green('VALIDATION COMPLETED')
			}
		}
    }
}

def loadConfigurationYML (Map config) {
    Boolean returnVal = true;
    returnVal = dir('/home/mgmtbld/demo_03/yml') {
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
