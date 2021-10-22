package org.devops
import org.devops.Constants


def dockerBuildPush(){
    //def HARBOR_HOST = "https://192.168.31.102/"
    def BuName = jobName.split('-')[0] //业务名
    def appName = jobName.split('-')[1] //应用名
    def commitId= sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
    //拼接镜像名
    imageName = "${Constants.HARBOR.find(/[0-9.]+/)}/${BuName}/${appName}:${commitId}"
    echo harbor
    //登陆harbor
    sh """
        #登陆
        docker login -u${HARBOR_CRED_USR} -p${HARBOR_CRED_PSW} ${harbor}
        
        #构建镜像
        docker build -t ${imageName} .
        
        #上传镜像
        docker push ${imageName}
    """                    
}