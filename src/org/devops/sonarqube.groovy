package org.devops
import org.devops.Constants


def HttpReq(httpMode,apiUrl,requestBody){
    result = httpRequest authentication             :Constants.SONARQUBE_CRED,
                            httpMode                :httpMode, 
                            contentType             :"APPLICATION_JSON",
                            consoleLogResponseBody  :true, 
                            ignoreSslErrors         :true, 
                            requestBody             :requestBody,
                            url                     :"${Constants.SONAR_SERVER}/${apiUrl}"
    return result
}

def ProjectExist(projectName){
    apiUrl = "api/projects/search?projects=${projectName}"
    response = HttpReq("GET",apiUrl,'')
    response = readJSON text: """${response.content}"""
    result = response["paging"]["total"]
    if (result == 0){
        return false
    }else{
        return true
    }
}

def CreateProject(projectName){
    apiUrl = "api/projects/create?name=${projectName}&project=${projectName}"
    response = HttpReq("POST",apiUrl,'')
    response = readJSON text: """${response.content}"""
    println response
}

def AssociateProjectQualityprofile(Map config = [:]){
    apiUrl = "api/qualityprofiles/add_project?language=${config.sonarQualityLang}&project=${config.projectName}&qualityProfile=${config.sonarQualityProfile}"
    response = HttpReq("POST",apiUrl,'')
}

def GetQgId(qualityGateName){
    apiUrl = "api/qualitygates/show?name=${qualityGateName}"
    response = HttpReq("GET",apiUrl,'')
    response = readJSON text: """${response.content}"""
    println response
    return response["id"]
}

def AssociateProjectQualitygate(Map config = [:]){
    gateId = GetQgId(config.qualityGateName)
    apiUrl = "api/qualitygates/select?projectKey=${config.projectName}&gateId=${gateId}"
    response = HttpReq("POST",apiUrl,'')
}

def ScanMaven(Map config = [:]){
    def sonarDate = sh(returnStdout: true, script: 'date +%Y%m%d%H%M%S').trim()

    if(ProjectExist(config.projectName) == false){
        println
        CreateProject(config.projectName)
        if(config.sonarQualityProfile){
            AssociateProjectQualityprofile(
                sonarQualityLang    :config.sonarQualityLang,
                sonarQualityProfile :config.sonarQualityProfile,
                projectName         :config.projectName
            )
        }
        if(config.qualityGateName){
            AssociateProjectQualitygate(
                projectName     :config.projectName,
                qualityGateName :config.qualityGateName
            )
        }
    }

    sh """ 
    sonar-scanner \
      -Dsonar.host.url=${Constants.SONAR_SERVER} \
      -Dsonar.projectKey=${config.projectName} \
      -Dsonar.projectName=${config.projectName} \
      -Dsonar.projectVersion=${sonarDate} \
      -Dsonar.login=${SONARQUBE_TOKEN} \
      -Dsonar.ws.timeout=30 \
      -Dsonar.projectDescription=${config.projectName} \
      -Dsonar.links.homepage=${gitUrl} \
      -Dsonar.links.ci=${env.BUILD_URL} \
      -Dsonar.sources=src/ \
      -Dsonar.sourceEncoding=UTF-8 \
      -Dsonar.java.binaries=target/classes \
      -Dsonar.java.test.binaries=target/test-classes \
      -Dsonar.java.surefire.report=target/surefire-reports \
      -Dsonar.gitlab.project_id=${projectId} \
      -Dsonar.gitlab.ref_name=${branch} \
      -Dsonar.gitlab.commit_sha=${commitSha} \
      -Dsonar.dynamicAnalysis=reuseReports \
      -Dsonar.gitlab.failure_notification_mode=commit-status \
      -Dsonar.gitlab.url=${Constants..GITLAB_HOST} \
      -Dsonar.gitlab.user_token=${GITLAB_CRED} \
      -Dsonar.gitlab.api_version=v4
    """
}

def ScanNpm(Map config = [:]){
    def sonarDate = sh(returnStdout: true, script: 'date +%Y%m%d%H%M%S').trim()
    println 'debug0'
    if(ProjectExist(config.projectName) == false){
        CreateProject(config.projectName)
        if(config.sonarQualityProfile){
            AssociateProjectQualityprofile(
                sonarQualityLang:config.sonarQualityLang,
                sonarQualityProfile:config.sonarQualityProfile,
                projectName:config.projectName                
            )
        }
        println 'debug1'
        if(config.qualityGateName){
            AssociateProjectQualitygate(
                projectName:config.projectName,
                qualityGateName:config.qualityGateName
            )
        }
    }
    sh """
      sonar-scanner \
      -Dsonar.host.url=${Constants.SONAR_SERVER} \
      -Dsonar.projectKey=${config.projectName} \
      -Dsonar.projectName=${config.projectName} \
      -Dsonar.projectVersion=${sonarDate} \
      -Dsonar.login=${SONARQUBE_TOKEN} \
      -Dsonar.ws.timeout=30 \
      -Dsonar.projectDescription=${config.projectName} \
      -Dsonar.links.homepage=${gitUrl} \
      -Dsonar.links.ci=${env.BUILD_URL} \
      -Dsonar.sources=src/ \
      -Dsonar.sourceEncoding=UTF-8 \
      -Dsonar.gitlab.project_id=${projectId} \
      -Dsonar.gitlab.ref_name=${branch} \
      -Dsonar.gitlab.commit_sha=${commitSha} \
      -Dsonar.dynamicAnalysis=reuseReports \
      -Dsonar.gitlab.failure_notification_mode=commit-status \
      -Dsonar.gitlab.url=${Constants.GITLAB_HOST} \
      -Dsonar.gitlab.user_token=${GITLAB_CRED} \
      -Dsonar.gitlab.api_version=v4
    """    
}


//get the result of the scanning
def getProjectStatus(){
    apiUrl = "api/project_branches/list?project=${jobName}"
    response = HttpReq("GET",apiUrl,'')
    result = readJSON text: """${response.content}"""
    return result["branches"][0]["status"]["qualityGateStatus"]
}

def SonarScan(Map config = [:]){
    switch(buildType){
        case 'mvn':
            ScanMaven(
                sonarQualityLang:config.sonarQualityLang,
                sonarQualityProfile:config.sonarQualityProfile,
                projectName:config.projectName,
                qualityGateName:config.qualityGateName
            )
            break
        case 'npm':
            ScanNpm(
                sonarQualityLang:config.sonarQualityLang,
                sonarQualityProfile:config.sonarQualityProfile,
                projectName:config.projectName,
                qualityGateName:config.qualityGateName
            )
            break
    }
    if(getProjectStatus() == 'ERROR'){
        error "代码质量扫描失败"
    }
}