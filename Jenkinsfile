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
        string(
            name: 'EMAIL_RECIPIENTS',
            defaultValue: '',
            description: 'Comma-separated list of email addresses to receive the test report'
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

    // Automatically trigger on every GitHub push; full test run requires a manual build
    triggers {
        githubPush()
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

        // Compile-only verification — runs on every push to catch build-break early
        stage('Compile') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn clean compile -DskipTests'
                    } else {
                        bat 'mvn clean compile -DskipTests'
                    }
                }
            }
        }

        stage('Setup Appium Driver') {
            when { not { triggeredBy 'GitHubPushCause' } }
            steps {
                bat 'appium driver install uiautomator2 || echo UIAutomator2 already installed'
            }
        }

        stage('Start Appium') {
            when { not { triggeredBy 'GitHubPushCause' } }
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
            when { not { triggeredBy 'GitHubPushCause' } }
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
            when { not { triggeredBy 'GitHubPushCause' } }
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

                // Send email report only on manual builds (not GitHub push triggers)
                def isGitHubPush = currentBuild.getBuildCauses('com.cloudbees.jenkins.GitHubPushCause').size() > 0
                if (!isGitHubPush && params.EMAIL_RECIPIENTS?.trim()) {
                    def buildStatus  = currentBuild.currentResult ?: 'UNKNOWN'
                    def statusColour = buildStatus == 'SUCCESS' ? '#2ecc71' : (buildStatus == 'UNSTABLE' ? '#f39c12' : '#e74c3c')
                    def subject      = "[Jenkins] RR Mobile Automation — ${buildStatus} — Build #${env.BUILD_NUMBER}"
                    def body = """
<html>
<body style="font-family:Arial,sans-serif;font-size:14px;color:#333;">

  <h2 style="color:${statusColour};">RR Mobile Automation — ${buildStatus}</h2>

  <table border="0" cellpadding="6" cellspacing="0" style="border-collapse:collapse;">
    <tr><td><b>Job</b></td><td>${env.JOB_NAME}</td></tr>
    <tr><td><b>Build #</b></td><td>${env.BUILD_NUMBER}</td></tr>
    <tr><td><b>Duration</b></td><td>${currentBuild.durationString}</td></tr>
    <tr><td><b>Branch</b></td><td>${env.GIT_BRANCH ?: 'N/A'}</td></tr>
    <tr><td><b>Device</b></td><td>${params.DEVICE_NAME} (${params.DEVICE_UDID})</td></tr>
    <tr><td><b>Platform</b></td><td>Android ${params.PLATFORM_VERSION}</td></tr>
    <tr><td><b>Suite</b></td><td>${params.SUITE_FILE}</td></tr>
  </table>

  <h3>Test Summary</h3>
  \${TEST_COUNTS, var="total"} tests run &nbsp;|&nbsp;
  <span style="color:#2ecc71;">\${TEST_COUNTS, var="pass"} passed</span> &nbsp;|&nbsp;
  <span style="color:#e74c3c;">\${TEST_COUNTS, var="fail"} failed</span> &nbsp;|&nbsp;
  <span style="color:#f39c12;">\${TEST_COUNTS, var="skip"} skipped</span>

  <h3>Links</h3>
  <ul>
    <li><a href="${env.BUILD_URL}">Build Console</a></li>
    <li><a href="${env.BUILD_URL}testReport/">Test Report</a></li>
    <li><a href="${env.BUILD_URL}Extent_Test_Report/">Extent HTML Report</a></li>
  </ul>

  <p style="color:#888;font-size:12px;">This email was generated automatically by Jenkins.</p>
</body>
</html>
"""
                    emailext(
                        subject:            subject,
                        body:               body,
                        mimeType:           'text/html',
                        to:                 params.EMAIL_RECIPIENTS,
                        attachmentsPattern: 'test-output/reports/*.html',
                        attachLog:          false
                    )
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