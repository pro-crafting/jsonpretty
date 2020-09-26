#!/usr/bin/env groovy

pipeline {
    agent any
    tools {
        maven 'maven-default'
        jdk 'openjdk11-zulu'
    }
    stages {
        stage ('Checking commit message') {
            when {
                allOf {
                    not {
                        buildingTag()
                    }
                    changelog '.*\\[maven-release-plugin\\].*'
                }
            }

            steps {
                script {
                  currentBuild.result = 'NOT_BUILT'
               }
               error('Skipping release build')
            }
        }
        stage ('Build') {
            steps {
                sh 'mvn install -P build-extras,jenkins-ci'
            }
        }
        stage ('Deploy') {
            when {
                not {
                    changeRequest()
                }
            }
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'QUARKUS_CONTAINER-IMAGE_USERNAME', passwordVariable: 'QUARKUS_CONTAINER-IMAGE_PASSWORD'),
                    usernamePassword(credentialsId: 'ossrh', usernameVariable: 'OSSRH_USERNAME', passwordVariable: 'OSSRH_TOKEN'),
                    usernamePassword(credentialsId: 'gpg', usernameVariable: 'GPG_KEY_NAME', passwordVariable: 'GPG_PASSPHRASE'),
                    file(credentialsId: 'mavensigningkey', variable: 'MAVEN_SIGNING_KEY')
                ]) {
                    sh "gpg --batch --fast-import ${env.MAVEN_SIGNING_KEY}"
                    sh 'mvn -DskipTests -Dquarkus.container-image.push=true deploy -s cd/settings.xml -P sign,build-extras'
                }
            }
        }
        stage ('Qualitiy - Sonar') {
            steps {
                withCredentials([
                    string(credentialsId: 'pro-crafting-sonarcloud', variable: 'SONARCLOUD_TOKEN')
                ]) {
                    sh "mvn org.jacoco:jacoco-maven-plugin:prepare-agent org.apache.maven.plugins:maven-surefire-plugin:test org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.login=${env.SONARCLOUD_TOKEN}"
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}