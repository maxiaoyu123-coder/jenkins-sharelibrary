package org.devops

class Constants{
    static String SONAR_SERVER = "http://192.168.31.102:9000"
    static String SONAR_SCANNER_HOME = "/opt/sonar-scanner-4.2.0.1873-linux"
    static String SONARQUBE_TOKEN = 'sonarqube-root-token'
    static String NEXUS_CRED = 'nexus-jenkins-password'
    static String HARBOR_CRED = 'harbor-admin-password'
    static String SONARQUBE_CRED = 'sonarqube-admin-password'
    static String GITLAB_CRED = 'gitlab-token'
    static String NPM_GROUP_REPO = "http://192.168.31.102:8081/repository/npm-group/"
    static String NPM_HOSTED_REPO = "http://192.168.31.102:8081/repository/npm-hosted/"

    static String GITLAB_HOST = "http://192.168.31.102"
    static String HARBOR = "https://192.168.31.102/"

    //servers.server.id in settings.xml for authentication to nexus repo
    static String MAVEN_GROUP_REPO = ""
    static String MAVEN_HOSTED_REPO = ""
    //jobName = JOB_NAME.contains('/')?JOB_NAME.split('/')[-1]:JOB_NAME
}



