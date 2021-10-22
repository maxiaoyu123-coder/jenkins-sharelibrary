package org.devops
import org.devops.Constants

//拉取代码
def checkOut(){
    branch = branch - 'refs/heads/'
    currentBuild.description = "triggered by ${gitUser}, branch ${branch}"
    git branch: "${branch}", 
        credentialsId: 'git-jenkins-password', 
        url: 'http://192.168.31.102/root/simple-java-maven-app-master.git'
}

//构建指令
def build(Map config = [:]){
    def buildTools = [
        'mvn': 'mvn clean package -DskipTests',
        'npm': "npm config set registry ${config.NPM_GROUP_REPO} \
                && npm config set _auth \$(echo -n '${config.NEXUS_CRED_USR}:${config.NEXUS_CRED_PSW}' | openssl base64) \
                && npm install && npm run build && npm pack"
    ]
    sh buildTools[config.buildType]
}

//打印颜色文字
def PrintMes(value,color){
    colors = ['red'   : "\033[40;31m >>>>>>>>>>>${value}<<<<<<<<<<< \033[0m",
              'blue'  : "\033[47;34m ${value} \033[0m",
              'green' : "[1;32m>>>>>>>>>>${value}>>>>>>>>>>[m",
              'green1' : "\033[40;32m >>>>>>>>>>>${value}<<<<<<<<<<< \033[0m" ]
    ansiColor('xterm') {
        println(colors[color])
    }
}

//http请求
def HttpReq(reqType,reqUrl,reqBody){
    result = httpRequest customHeaders: [[maskValue: true, name: 'PRIVATE-TOKEN', value: gitlabToken]], 
            httpMode: reqType,
            contentType: 'APPLICATION_JSON',
            consoleLogResponseBody: true,
            ignoreSslErrors: true,
            requestBody: "${reqBody}",
            url: "${reqUrl}"
    return result
}

//调用gitlab RestAPI, 发送提交结果
def ChangeCommitStatus(projectId,commitSha,status){
    api = "api/v4/projects/${projectId}/statuses/${commitSha}?state=${status}"
    url = "http://192.168.31.102/${api}"
    response = HttpReq('POST',url,'')
    print response
    return response
}