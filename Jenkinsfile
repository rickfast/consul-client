pipeline {
    agent any
    stages {
        stage('Build and publish') {
            steps {
                sh "docker run --rm -i -v ${env.WORKSPACE}:/app -v ${env.HOME}/.aws:/root/.aws -w /app maven:3.8.3-openjdk-11-slim ./build.sh"
            }
        }
    }
}
