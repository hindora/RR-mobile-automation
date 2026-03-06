pipeline {

    agent any

    // ------------------------------------------------------------------
    // Build parameters — override defaults per-run from the Jenkins UI
    // ------------------------------------------------------------------
    parameters {
        string(
            name: 'DEVICE_UDID',
            defaultValue: 'adb-R5CRA1JVYWL-3TRgFc._adb-tls-connect._tcp',
            description: 'ADB device UDID — run: adb devices'
        )
        string(
            name: 'DEVICE_NAME',
            defaultValue: 'Galaxy s21',
            description: 'Friendly device name (used in Appium capabilities)'
        )
        string(
            name: 'PLATFORM_VERSION',
            defaultValue: '12',
            description: 'Android OS version on the device'
        )
        string(
            name: 'APPIUM_PORT',
            defaultValue: '4723',
            description: 'Port Appium server listens on'
        )
        string(
            name: 'SUITE_FILE',
            defaultValue: 'testng.xml',
            description: 'TestNG suite XML file to execute'
        )
    }

    // ------------------------------------------------------------------
    // Tool aliases — must match names in Jenkins → Global Tool Config
    // ------------------------------------------------------------------
    tools {
        jdk   'JDK21'   // Configure: Manage Jenkins → Tools → JDK → name = "JDK21"
        maven 'MAVEN3'  // Configure: Manage Jenkins → Tools → Maven → name = "MAVEN3"
    }

    environment {
        PATH = "C:\\Users\\harii\\AppData\\Roaming\\npm;${env.PATH}"
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 60, unit: 'MINUTES')
    }

    // ------------------------------------------------------------------
    // Stages
    // ------------------------------------------------------------------
    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Start Appium') {
            steps {
                script {
                    if (isUnix()) {
                        sh """
                            nohup appium --port ${params.APPIUM_PORT} \\
                                --log "\${WORKSPACE}/appium.log" \\
                                --log-timestamp &
                            echo \$! > "\${WORKSPACE}/appium.pid"
                            sleep 5
                        """
                    } else {
                        // Windows — start Appium in a detached background window
                        bat """
                            start "Appium" /B appium --port ${params.APPIUM_PORT} --log "%WORKSPACE%\\appium.log" --log-timestamp
                            powershell -Command "Start-Sleep -Seconds 5"
                        """
                    }
                }
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    if (isUnix()) {
                        sh """
                            mvn clean test \\
                                -Dsurefire.suiteXmlFiles=${params.SUITE_FILE} \\
                                "-Ddevice.udid=${params.DEVICE_UDID}" \\
                                "-Ddevice.name=${params.DEVICE_NAME}" \\
                                -Dplatform.version=${params.PLATFORM_VERSION} \\
                                -Dappium.server.url=http://127.0.0.1:${params.APPIUM_PORT}
                        """
                    } else {
                        bat """
                            mvn clean test ^
                                -Dsurefire.suiteXmlFiles=${params.SUITE_FILE} ^
                                "-Ddevice.udid=${params.DEVICE_UDID}" ^
                                "-Ddevice.name=${params.DEVICE_NAME}" ^
                                -Dplatform.version=${params.PLATFORM_VERSION} ^
                                -Dappium.server.url=http://127.0.0.1:${params.APPIUM_PORT}
                        """
                    }
                }
            }
        }

        stage('Publish Reports') {
            steps {
                // TestNG results
                testNG reportFilenamePattern: 'target/surefire-reports/testng-results.xml'

                // JUnit-compatible XML (picked up by Jenkins test trend graphs)
                junit allowEmptyResults: true,
                      testResults: 'target/surefire-reports/junitreports/*.xml'

                // ExtentReports HTML (filename includes timestamp, so use wildcard)
                publishHTML([
                    allowMissing:          true,
                    alwaysLinkToLastBuild: true,
                    keepAll:               true,
                    reportDir:             'test-output/reports',
                    reportFiles:           '*.html',
                    reportName:            'Extent Test Report'
                ])
            }
        }
    }

    // ------------------------------------------------------------------
    // Post-build
    // ------------------------------------------------------------------
    post {
        always {
            script {
                // Stop Appium server
                if (isUnix()) {
                    sh "[ -f \"\${WORKSPACE}/appium.pid\" ] && kill \$(cat \"\${WORKSPACE}/appium.pid\") 2>/dev/null || true"
                } else {
                    bat "taskkill /F /FI \"WINDOWTITLE eq Appium\" /T 2>NUL || exit 0"
                }
            }

            // Archive screenshots and Appium log for inspection
            archiveArtifacts artifacts: 'test-output/screenshots/**/*.png',
                              allowEmptyArchive: true

            archiveArtifacts artifacts: 'appium.log',
                              allowEmptyArchive: true
        }

        success {
            echo 'All tests passed.'
        }

        failure {
            echo 'Tests failed — check the Extent Report and screenshots above.'
        }
    }
}