package org.devops
import org.devops.Constants

def upload(){
    switch(buildType){
        case 'mvn':
            jarName = sh(returnStdout: true, script: "cd target; ls *.jar").trim()
            pom = readMavenPom file: 'pom.xml'
            pomGroupId = "${pom.groupId}"
            pomArtifact = "${pom.artifactId}"
            pomVersion = "${pom.version}"
            pomPackaging = "${pom.packaging}"
            sh  """
              cd target
              //修改成使用curl命令上传
              mvn deploy:deploy-file \
                -Dmaven.test.skip=true \
                -Dfile=${jarName} \
                -DgroupId=${pomGroupId} \
                -DartifactId=${pomArtifact} \
                -Dversion=${pomVersion} \
                -Dpackaging=${pomPackaging} \
                -DrepositoryId=${Constants.MAVEN_HOSTED_REPO} \
                -Durl=${Constants.MAVEN_HOSTED_REPO}
            """
            break
        case 'npm' :
            sh """
                npm config set registry ${Constants.NPM_HOSTED_REPO} \
                && npm config set _auth \$(echo -n '${NEXUS_CRED_USR}:${NEXUS_CRED_PSW}' | openssl base64) \
                npm --registry=${Constants.NPM_HOSTED_REPO} publish *.tgz
            """
            break 
    }
}