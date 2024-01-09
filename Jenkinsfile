@Library('jenkins_library@trustitaly') import ipzs.properties.projectProperties

pipeline {
	agent {
    	kubernetes {
      	    cloud config.cloudSelector(BRANCH_NAME)
            defaultContainer 'jenkins-builder'
    	}
  	}
    environment {
        BUILDER = 'tiny-jammy'
        TBS_SERVICE_ACCOUNT = 'jenkins-cicd-tools-sa'
        PROJECT_NAME = 'trustitaly'
        APP_NAME = 'archetype-db-connector'
    }
    options {
        ansiColor('xterm')
        disableConcurrentBuilds()
        gitLabConnection('git.ipzs.it')
    }
    stages {
        stage('Properties configuration') {
            steps {
                script {
                    updateGitlabCommitStatus name: 'Pipeline', state: 'running'
                    updateGitlabCommitStatus name: 'Init', state: 'running'
                    // Environment
                    env.targetEnv = config.envSelector(BRANCH_NAME)
                    objectProject = new projectProperties(APP_NAME, PROJECT_NAME)
                    // Emails
                    env.developersDL = objectProject.getDevelopersDL()
                    env.securityDL = objectProject.getSecurityDL()
                    env.GIT_USER_NAME = gitUtils.gitCommitUser()
                    env.GIT_USER_EMAIL = gitUtils.gitCommitEmail()
                }
            }
            post {
                success { updateGitlabCommitStatus name: 'Init', state: 'running' }
                failure { 
                    updateGitlabCommitStatus name: 'Init', state: 'failed'
                    script { currentBuild.result = 'FAILURE'}
                }
            }
        }
        stage('Release Tag retrieve') {
            steps {
                script {
                    env.SHORT_GIT_COMMIT = GIT_COMMIT.substring(0, 6)
                    env.RELEASE_VERSION = gitUtils.getGitVersion()
                }
            }
            post {
                success { updateGitlabCommitStatus name: 'Init', state: 'running' }
                failure { 
                    updateGitlabCommitStatus name: 'Init', state: 'failed'
                    script { currentBuild.result = 'FAILURE'}
                }
            }
        }
        stage('Nexus Configuration') {
            steps {
                script {
                    artifact.nexusConfiguration(objectProject)
                }
            }
            post {
                success { updateGitlabCommitStatus name: 'Init', state: 'success' }
                failure { 
                    updateGitlabCommitStatus name: 'Init', state: 'failed'
                    script { currentBuild.result = 'FAILURE'}
                }
            }
        }
        stage('[SAST] Secrets Auditing') {
            steps {
                script {
                    updateGitlabCommitStatus name: 'Secrets Auditing', state: 'running'
                    try {
                        env.secrestAuditingFailed = false
                        sast.secretsAuditing()
                    } catch(Exception e) {
                        updateGitlabCommitStatus name: 'Secrets Auditing', state: 'failed' 
                        env.secrestAuditingFailed = true
                        emailRecipients = [GIT_USER_EMAIL, developersDL]
                        requiredInput.gitleaksInput(objectProject, 'report-gitleaks.json', emailRecipients)
                    }
                }
            }
            post {
                success { 
                    script {
                        if (secrestAuditingFailed) { updateGitlabCommitStatus name: 'Secrets Auditing', state: 'failed' 
                        } else { updateGitlabCommitStatus name: 'Secrets Auditing', state: 'success' }
                    }
                }
                failure { 
                    updateGitlabCommitStatus name: 'Secrets Auditing', state: 'failed'
                    script { currentBuild.result = 'FAILURE' }
                }
            }
        }
        stage('Artifact Build') {
            steps {
                script {
                    updateGitlabCommitStatus name: 'Artifact Build', state: 'running'
                    artifact.build("mvnArch")
                }
            }
            post {
                success { updateGitlabCommitStatus name: 'Artifact Build', state: 'success' }
                failure { 
                    updateGitlabCommitStatus name: 'Artifact Build', state: 'failed'
                    script { currentBuild.result = 'FAILURE'}
                }
            }
        }
        stage('[SAST] SBOM File Generation') {
            steps {
                script {
                    sast.sbomGeneration()
                }
            }
            post {
                success { updateGitlabCommitStatus name: 'Code Analysis', state: 'running' }
                failure { 
                    updateGitlabCommitStatus name: 'Code Analysis', state: 'failed'
                    script { currentBuild.result = 'FAILURE'}
                }
            }
        }
        stage('SonarQube Analysis') {
            steps {
                updateGitlabCommitStatus name: 'SonarQube Code Analysis', state: 'running'
                withSonarQubeEnv(credentialsId: 'sonarqube-global-token', installationName: 'IPZS-SonarQube-Server') {
                    sh 'mvn clean verify sonar:sonar -Pcoverage -Dsonar.projectKey=$APP_NAME'
                }
            }
            post {
                success { 
                    updateGitlabCommitStatus name: 'SonarQube Code Analysis', state: 'success' 
                    println("Check the overall situation for this service here: https://sonarqube.ipzs.it/dashboard?id=${APP_NAME}")
                }
                failure { 
                    updateGitlabCommitStatus name: 'SonarQube Code Analysis', state: 'failed'
                    script { currentBuild.result = 'FAILURE'}
                }
            }
        }
        stage('Artifact Tests') {
            steps {
                script {
                    updateGitlabCommitStatus name: 'Artifact Tests', state: 'running'
                    artifact.tests("mvnArch")
                    junit 'target/test-classes/projects/basic/project/hello-world/target/surefire-reports/TEST-*.xml'
                }
            }
            post {
                success { updateGitlabCommitStatus name: 'Artifact Tests', state: 'success' }
                failure {
                    updateGitlabCommitStatus name: 'Artifact Tests', state: 'failed'
                    script { currentBuild.result = 'FAILURE'}
                }
            }
        }
        stage('Artifact Push to Nexus') {
            steps {
                script {
                    updateGitlabCommitStatus name: 'Artifact Push', state: 'running'
                    sh 'mvn clean deploy'
                }
            }
            post {
                success { updateGitlabCommitStatus name: 'Artifact Push', state: 'success' }
                failure { 
                    updateGitlabCommitStatus name: 'Artifact Push', state: 'failed'
                    script { currentBuild.result = 'FAILURE'}
                }
            }
        }
    }
    post {
        always {
            script {
                currentBuild.displayName = "#${BUILD_NUMBER}, RELEASE: ${RELEASE_VERSION}, BRANCH: ${BRANCH_NAME}"
            }
        }
        success {
            updateGitlabCommitStatus name: 'Pipeline', state: 'success' 
        }
        unstable {
            script {
                if (qualityGateStatus != "OK") { updateGitlabCommitStatus name: 'Pipeline', state: 'success' }
                else { updateGitlabCommitStatus name: 'Pipeline', state: 'canceled' }
            }
        }
        aborted {
            updateGitlabCommitStatus name: 'Pipeline', state: 'failed' 
        }
        failure {
            updateGitlabCommitStatus name: 'Pipeline', state: 'failed'
            script {
                if ( targetEnv in ['prod', 'uat', 'dev'] ) {
                    recipientsEmail = [GIT_USER_EMAIL, developersDL]
                    email.pipelineFailed(recipientsEmail)
                }
            }
        }
    }
}
