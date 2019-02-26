#!groovy
def call(){
    pipeline {
    agent any
  /*  tools {
        jdk 'JAVA_8'
    } */

    parameters {
   
     	booleanParam(name: 'CODE_SCANS_REQUIRED', defaultValue: true, description: 'Execute Code Scans?')        
        booleanParam(name: 'PUBLISH_RELEASE', defaultValue: false, description: 'Publish Release Artifact to Maven Repo')
        choice(name: 'DEPLOY_TO', choices: '\nRelease\nStaging\nProduction', description: 'Which level / space?')
        choice(name: 'DATA_CENTER', choices: 'EDC\nWTC\nEDC and WTC', description: 'Which data center?') 
       
    }

    environment {

        APP_NM="clsd"
        APP_VERSION_SNAPSHOT = "12.0.0-SNAPSHOT"
      	APP_VERSION_RELEASE="12.0.0.${env.BUILD_NUMBER}"
        ROUTE_PATH="clsd/v12"

        LATEST_PROD_RELEASE='12.0.0.0'
        LATEST_PROD_ROUTE_PATH="clsd/v12"

        JDK_VERSION = "jdk8"

		REPO_WITH_BINARIES_SNAPSHOT="https://nexus.prod.cloud.fedex.com:8443/nexus/content/repositories/802682-CLSD-snapshot";
        REPO_WITH_BINARIES_RELEASE="https://nexus.prod.cloud.fedex.com:8443/nexus/content/repositories/802682-CLSD"
		REPO_WITH_BINARIES_CREDENTIAL_ID = "802682_admin"

		//development credentials
        PAAS_DEVELOPMENT_CREDENTIAL_ID = "FXSTEMP_app802682"       
        PAAS_DEVELOPMENT_API_URL = "https://api.sys.wtcdev2.paas.fedex.com"       
        PAAS_DEVELOPMENT_ORG = "802682"
        PAAS_DEVELOPMENT_SPACE = "development"
        
        //release credentials
        PAAS_TEST_CREDENTIAL_ID = "FXSTEMP_app802682"
        PAAS_TEST_API_URL = "https://api.sys.wtcdev2.paas.fedex.com"
        PAAS_TEST_ORG = "802682"
        PAAS_TEST_SPACE = "release"
        PAAS_HOSTNAME_UUID = ""
        JAVA_BUILDPACK_URL = "https://github.com/cloudfoundry/java-buildpack.git#v3.8.1"
        PIPELINE_DESCRIPTOR = "CLSD PCF pipeline"
        
        //Staging credentials
        PAAS_STAGING_WTC_CREDENTIAL_ID = "FXSTEMP_app802682"
        PAAS_STAGING_WTC_API_URL = "https://api.sys.wtccf1.paas.fedex.com"
        PAAS_STAGING_WTC_ORG = "802682"
        PAAS_STAGING_WTC_SPACE = "staging"
		
        PAAS_STAGING_EDC_CREDENTIAL_ID = "FXSTEMP_app802682"
        PAAS_STAGING_EDC_API_URL = "https://api.sys.edccf1.paas.fedex.com"
        PAAS_STAGING_EDC_ORG = "802682"
        PAAS_STAGING_EDC_SPACE = "staging"
        
        
        //Production credentials
        PAAS_PRODUCTION_WTC_CREDENTIAL_ID = "FXSTEMP_app802682"
        PAAS_PRODUCTION_WTC_API_URL = "https://api.sys.wtccf1.paas.fedex.com"
        PAAS_PRODUCTION_WTC_ORG = "802682"
        PAAS_PRODUCTION_WTC_SPACE = "production"
		
        PAAS_PRODUCTION_EDC_CREDENTIAL_ID = "FXSTEMP_app802682"
        PAAS_PRODUCTION_EDC_API_URL = "https://api.sys.edccf1.paas.fedex.com"
        PAAS_PRODUCTION_EDC_ORG = "802682"
        PAAS_PRODUCTION_EDC_SPACE = "production"
        

		REPO_SUBDIR="CLSD"
        PCF_SUBPROJECT="CLSD_Cloud"
        MANIFEST_DIR="CLSD_Cloud/deploy"

        MAIL_LIST="ashok.obilisetty.osv@fedex.com"
        
        RELEASE_MAIL_LIST="abhijeet.patil.osv@fedex.com"
        RELEASE_APPROAVAL_LIST="741982"
        
        STAGING_MAIL_LIST="abhijeet.patil.osv@fedex.com"
        STAGING_APPROAVAL_LIST="741982"
        
        PROD_MAIL_LIST="abhijeet.patil.osv@fedex.com"
        PROD_APPROAVAL_LIST="741982"
        
        // Timeout values in minutes
		RELEASE_TIME_OUT=1
		STAGE_TIME_OUT=10
		PROD_TIME_OUT=10
		
    }

    stages {
    
        stage("Build and Upload") {
    
            steps {
            
             echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"

              /*  sh """#!/bin/bash
                pushd \${WORKSPACE}/${REPO_SUBDIR}

                rm -f archive.tar.gz*

                wget https://gitlab.prod.fedex.com/436498/apptx-pipelines/repository/master/archive.tar.gz

                mkdir -p ci && tar -xzf archive.tar.gz -C ci --strip-components 1

                popd

                """ 

                script {

                    if (env.REPO_WITH_BINARIES_CREDENTIAL_ID) {
                        withCredentials([usernamePassword(credentialsId: env.REPO_WITH_BINARIES_CREDENTIAL_ID, passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                            env.M2_SETTINGS_REPO_USERNAME = USER
                            env.M2_SETTINGS_REPO_PASSWORD = PASS
                        }
                    }

                    if (env.PAAS_DEVELOPMENT_CREDENTIAL_ID) {
                        withCredentials([usernamePassword(credentialsId: env.PAAS_DEVELOPMENT_CREDENTIAL_ID, passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                            env.PAAS_DEVELOPMENT_USERNAME = USER
                            env.PAAS_DEVELOPMENT_PASSWORD = PASS
                        }
                    }

                    if (env.PAAS_TEST_CREDENTIAL_ID) {
                        withCredentials([usernamePassword(credentialsId: env.PAAS_TEST_CREDENTIAL_ID, passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                                env.PAAS_TEST_USERNAME = USER
                                env.PAAS_TEST_PASSWORD = PASS
                        }
                    }

					

                    if (env.PAAS_STAGING_WTC_CREDENTIAL_ID) {
                        withCredentials([usernamePassword(credentialsId: env.PAAS_STAGING_WTC_CREDENTIAL_ID, passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                            env.PAAS_STAGING_WTC_USERNAME = USER
                            env.PAAS_STAGING_WTC_PASSWORD = PASS
                        }
                    }
                    
                    if (env.PAAS_STAGING_EDC_CREDENTIAL_ID) {
                        withCredentials([usernamePassword(credentialsId: env.PAAS_STAGING_EDC_CREDENTIAL_ID, passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                            env.PAAS_STAGING_EDC_USERNAME = USER
                            env.PAAS_STAGING_EDC_PASSWORD = PASS
                        }
                    }
                    
                     if (env.PAAS_PRODUCTION_WTC_CREDENTIAL_ID) {
                        withCredentials([usernamePassword(credentialsId: env.PAAS_PRODUCTION_WTC_CREDENTIAL_ID, passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                            env.PAAS_PRODUCTION_WTC_USERNAME = USER
                            env.PAAS_PRODUCTION_WTC_PASSWORD = PASS
                        }
                    }
                    
                    if (env.PAAS_PRODUCTION_EDC_CREDENTIAL_ID) {
                        withCredentials([usernamePassword(credentialsId: env.PAAS_PRODUCTION_EDC_CREDENTIAL_ID, passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                            env.PAAS_PRODUCTION_EDC_USERNAME = USER
                            env.PAAS_PRODUCTION_EDC_PASSWORD = PASS
                        }
                    }
                
                
                	if (DEPLOY_TO == 'Release') {
                    	
                    	env.DEPLOY_TO_TEST_STEP_REQUIRED = true
                    	env.DEPLOY_TO_STAGING_STEP_REQUIRED = false
                    	env.DEPLOY_TO_PRODUICTION_STEP_REQUIRED = false
                    
                	}
                	
                	if (DEPLOY_TO == 'Staging') {
                    	
                    	env.DEPLOY_TO_TEST_STEP_REQUIRED = true
                    	env.DEPLOY_TO_STAGING_STEP_REQUIRED = true
                    	env.DEPLOY_TO_PRODUICTION_STEP_REQUIRED = false
                    	
                    	env.PUBLISH_RELEASE=true
                    	
                    	if(DATA_CENTER == 'EDC and WTC') {
                    		env.ENVIRONMENT = 'STAGING_EDC'
                    	
                    	} else if(DATA_CENTER == 'EDC') {
                    	    env.ENVIRONMENT = 'STAGING_EDC'
                    	       
                    	} else if(DATA_CENTER == 'WTC') {
                    	    env.ENVIRONMENT = 'STAGING_WTC'   
                    	}
                	}
                	
                	if (DEPLOY_TO == 'Production') {
                    	
                    	env.DEPLOY_TO_TEST_STEP_REQUIRED = true
                    	env.DEPLOY_TO_STAGING_STEP_REQUIRED = true
                    	env.DEPLOY_TO_PRODUICTION_STEP_REQUIRED = true
                    	
                    	env.PUBLISH_RELEASE=true
                    	
                    	if(DATA_CENTER == 'EDC and WTC') {
                    		env.ENVIRONMENT = 'PRODUCTION_EDC'
                    	
                    	} else if(DATA_CENTER == 'EDC') {
                    	    env.ENVIRONMENT = 'PRODUCTION_EDC'
                    	       
                    	} else if(DATA_CENTER == 'WTC') {
                    	    env.ENVIRONMENT = 'PRODUCTION_WTC'   
                    	}
                    
                	}
                
                }

                sh '''#!/bin/bash
                ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/build_and_upload.sh
                '''

                //checkpoint('Completed Snapshot Build') */
				 echo "${params.DATA_CENTER}"
            }
        }
        
        stage("Code Quality Scanning") {
            when {
                environment name: 'CODE_SCANS_REQUIRED', value: 'true'
            }
            steps {
              /*  sh '''#!/bin/bash
                ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/code_scans.sh
                ''' */
				echo "${params.DATA_CENTER}"
            }
        }

        stage("Deploy to L2 / Development") {
            steps {           
            /*    sh '''#!/bin/bash
                ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/test_deploy.sh
                ''' */
				echo "${params.DATA_CENTER}"
            }
        }

        stage("Smoke Test on L2 / Development") {
            steps {
            /*    sh '''#!/bin/bash
                ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/test_smoke.sh
                '''
				*/
               // checkpoint('Completed Smoke Tests')
			   echo "${params.DATA_CENTER}"
            }
        }

        stage("Deploy to L3 / Release") {
            
            when {
                environment name: 'DEPLOY_TO_TEST_STEP_REQUIRED', value: 'true'
            }
            
            steps {
            
			echo "${params.DATA_CENTER}"
            /*	mail to: "${RELEASE_MAIL_LIST}", subject: "L3 / Release Deployment Approval", body: "Please check the build at ${BUILD_URL}."
            	
            	script {
            		
            		timeout(time: env.RELEASE_TIME_OUT.toInteger(), unit: 'MINUTES') {
						
						def y = input message:'Do you want to approve to deploy to L3?', id:'L3Approvals', ok: 'Approve it', 
						submitter: RELEASE_APPROAVAL_LIST, submitterParameter: 'approvalId', 
						parameters: [string(defaultValue: 'Approved', description: '', name: 'Comments')]
					
						echo("Approved by : " + y['approvalId'])
					}	

          		} 
                
                sh '''#!/bin/bash
                ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/stage_deploy.sh
                ''' */
            }
        }
        
        stage("End to end tests on L3 / Release") {
            when {
                environment name: 'DEPLOY_TO_TEST_STEP_REQUIRED', value: 'true'
            }
           steps {
		   
		   echo "${params.DATA_CENTER}"
            /*    sh '''#!/bin/bash
                ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/stage_e2e.sh
                '''  */
            }
        }
        
      /*  stage('Publish Release Approval') {
            
            // when {
            //     environment name: 'PUBLISH_RELEASE', value: 'true'
            // }
            
            steps {

               sh '''#!/bin/bash
                ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/publish_release.sh
                '''  
            }
         } 
         */
        
   /*     stage("Deploy to L6 / staging WTC") {
            
            when {
             	environment name: 'DEPLOY_TO_STAGING_STEP_REQUIRED', value: 'true'
            }
            
            steps {   
            
            	echo "Approval is required to deploy it to staging environment (STAGING_WTC)"     
            
            	mail to: "${STAGING_MAIL_LIST}", subject: "Staging deployment Approval (STAGING_WTC)", body: "Please check the build at ${BUILD_URL}."

            	script {
            		
            		timeout(time: env.STAGE_TIME_OUT.toInteger(), unit: 'MINUTES') {
						
						def y = input message:'Do you want to approve to deploy to L6 WTC?', id:'L6Approvals', ok: 'Approve it', 
						submitter: STAGING_APPROAVAL_LIST, submitterParameter: 'approvalId', 
						parameters: [string(defaultValue: 'Approved', description: '', name: 'Comments')]
					
						echo("Approved by : " + y['approvalId'])
					}	
					
					env.ENVIRONMENT = 'STAGING_WTC'

          		}
                     
				echo "Approved ... now deploying to staging (WTC)"
			
                sh '''#!/bin/bash
                ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/prod_deploy.sh
                '''  
            }
        } */
        
    /*    stage("L6 WTC - Complete switch over?") {
        
        	when {
             	environment name: 'DEPLOY_TO_STAGING_STEP_REQUIRED', value: 'true'
            }
            
			steps {
                script {
    
                    env.DEPLOY_TO_PROD = true
                    try{
                        input 'L6 WTC - Complete Switch Over?'
                    }catch(e){
                        env.DEPLOY_TO_PROD = false
                    }
                }

				sh '''#!/bin/bash

                    # TODO: Move this inside the scripts?             
                    if [ "${DEPLOY_TO_PROD}" == "true" ]
                    then
				        ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/prod_complete.sh
                    else
				        ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/prod_rollback.sh
                    fi
                        
				''' 
			}
		} */
        
      /*  stage("Deploy to staging EDC") {
            when {
            	environment name: 'DEPLOY_TO_STAGING_STEP_REQUIRED', value: 'true'
                 
            }
           steps {   
            
           	echo "Approval is required to deploy it to staging environment (STAGING_EDC)"     
            
            	mail to: "${STAGING_MAIL_LIST}", subject: "Staging deployment Approval (STAGING_EDC)", body: "Please check the build at ${BUILD_URL}."

            	script {
            		
            		timeout(time: env.STAGE_TIME_OUT.toInteger(), unit: 'MINUTES') {
						
						def y = input message:'Do you want to approve to deploy to L6 EDC?', id:'L6Approvals', ok: 'Approve it', 
						submitter: STAGING_APPROAVAL_LIST, submitterParameter: 'approvalId', 
						parameters: [string(defaultValue: 'Approved', description: '', name: 'Comments')]
					
						echo("Approved by : " + y['approvalId'])
					}	
					
					env.ENVIRONMENT = 'STAGING_WTC'

          		}
                     
				echo "Approved ... now deploying to staging (EDC)"
			
                sh '''#!/bin/bash
                ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/prod_deploy.sh
                ''' 
            }
        } */
        
    /*    stage("L6 EDC - Complete switch over?") {
        
        	when {
             	environment name: 'DEPLOY_TO_STAGING_STEP_REQUIRED', value: 'true'
            }
            
			steps {
                script {
    
                   env.DEPLOY_TO_PROD = true
                    try{
                        input 'L6 EDC - Complete Switch Over?'
                    }catch(e){
                        env.DEPLOY_TO_PROD = false
                    }
                }

				sh '''#!/bin/bash

                    # TODO: Move this inside the scripts?             
                    if [ "${DEPLOY_TO_PROD}" == "true" ]
                    then
				        ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/prod_complete.sh
                    else
				        ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/prod_rollback.sh
                    fi
                        
				''' 
			}
		} */
		
     /*   stage("Deploy to production WTC") {

            when {
            	environment name: 'DEPLOY_TO_PRODUICTION_STEP_REQUIRED', value: 'true'
            }

             steps {   
            
            	echo "Approval is required to deploy it to production environment (PRODUCTION_WTC)"     
            
            	mail to: "${PROD_MAIL_LIST}", subject: "Production deployment Approval (PRODUCTION_WTC)", body: "Please check the build at ${BUILD_URL}."

            	script {
            		
            		timeout(time: env.PROD_TIME_OUT.toInteger(), unit: 'MINUTES') {
						
						def y = input message:'Do you want to approve to deploy to Production WTC?', id:'ProdApprovals', ok: 'Approve it', 
						submitter: PROD_APPROAVAL_LIST, submitterParameter: 'approvalId', 
						parameters: [string(defaultValue: 'Approved', description: '', name: 'Comments')]
					
						echo("Approved by : " + y['approvalId'])
					}
					
					env.ENVIRONMENT = 'PRODUCTION_WTC'	

          		}
                     
				echo "Approved ... now deploying to production (WTC)"
			
                sh '''#!/bin/bash
                ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/prod_deploy.sh
                '''   
            }
        } */
        
     /*   stage("Deploy to production EDC") {
            when {
            	environment name: 'DEPLOY_TO_PRODUICTION_STEP_REQUIRED', value: 'true'
                 
            }
             steps {   
            
           	echo "Approval is required to deploy it to production environment (PRODUCTION_EDC)"     
            
            	mail to: "${PROD_MAIL_LIST}", subject: "Production deployment Approval (PRODUCTION_EDC)", body: "Please check the build at ${BUILD_URL}."

            	script {
            		
            		timeout(time: env.PROD_TIME_OUT.toInteger(), unit: 'MINUTES') {
						
						def y = input message:'Do you want to approve to deploy to Production EDC?', id:'ProdApprovals', ok: 'Approve it', 
						submitter: PROD_APPROAVAL_LIST, submitterParameter: 'approvalId', 
						parameters: [string(defaultValue: 'Approved', description: '', name: 'Comments')]
					
						echo("Approved by : " + y['approvalId'])
					}	
					
					env.ENVIRONMENT = 'PRODUCTION_EDC'

          		}
                     
				echo "Approved ... now deploying to production (EDC)"
			
                sh '''#!/bin/bash
                ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/prod_deploy.sh
                ''' 
            }
        } */
        
  /*      stage("Run GTM Production E2E") {
			
			when {
            	environment name: 'DEPLOY_TO_PRODUICTION_STEP_REQUIRED', value: 'true'
            }
            
			steps {
			
				sh '''#!/bin/bash
				#chmod 777 ${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/stage_prode2e.sh
				#${WORKSPACE}/${REPO_SUBDIR}/ci/common/src/main/bash/stage_prode2e.sh
				echo 'Running Production E2E'
				''' 
			}
		}*/
	} 
	
	post {
        always {
         //   junit '**/test-results/smoke/**/*.xml'
            // step([$class: 'JacocoPublisher'])
            // junit '**/test-results/smoke/**/*.xml'
			echo 'completed'
        }
        failure {
		//	mail to: "${MAIL_LIST}", subject: "Build ${env.BUILD_NUMBER} is failed", body: "Please check the build at ${BUILD_URL}."  
       echo 'failed'		
		}
		success {		     
		//	mail to: "${MAIL_LIST}", subject: "Build ${env.BUILD_NUMBER} is Successful", body: "Please check the build at ${BUILD_URL}."   
      echo 'completed successfuly'		
		    
		}

   	 }
}
}
