# RR Mobile Automation Framework

Production-grade Android mobile test automation framework built with Appium 2.x, TestNG, and Maven. Designed for parallel test execution against physical Android devices with full CI/CD integration via Jenkins.

---

## Tech Stack

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 21 | Language |
| Maven | 3.x | Build & dependency management |
| Appium | 2.x | Mobile automation driver |
| Appium Java Client | 8.6.0 | Java bindings for Appium |
| TestNG | 7.9.0 | Test framework |
| Allure | 2.27.0 | Test reporting |
| Log4j2 | 2.22.1 | Logging |
| Jackson | 2.16.1 | JSON test data |

---

## Project Structure

```
RR-mobile-automation/
├── Jenkinsfile.automation        # Job 2 — test execution pipeline
├── Jenkinsfile.build             # Job 1 — compile & package pipeline
├── testng.xml                    # TestNG suite configuration
├── pom.xml                       # Maven build configuration
└── src/
    ├── main/java/com/rr/mobile/
    │   ├── base/                 # BasePage — Page Object base class
    │   ├── config/               # ConfigReader, FrameworkConfig
    │   ├── driver/               # DriverFactory, DriverManager (ThreadLocal)
    │   ├── listeners/            # TestListener, RetryAnalyzer
    │   ├── pages/                # HomePage, LoginPage, ProfilePage, SettingsPage
    │   ├── services/             # LoginService
    │   └── utils/                # WaitUtils, GestureUtils, ScreenshotUtils, JsonDataReader
    └── test/
        ├── java/com/rr/mobile/
        │   ├── base/             # BaseTest — driver lifecycle management
        │   └── tests/            # HomeTest, LoginTest
        └── resources/
            ├── config/           # config.properties
            ├── testdata/         # testdata.json
            └── apps/             # APK files
```

---

## Prerequisites

- Java 21+
- Maven 3.x
- Node.js + npm
- Appium 2.x (`npm install -g appium`)
- Appium UIAutomator2 driver (`appium driver install uiautomator2`)
- Android SDK & ADB
- Physical Android device or emulator

---

## Configuration

All runtime settings are in `src/test/resources/config/config.properties`:

```properties
# Appium server
appium.server.url=http://127.0.0.1:4723

# Device
device.name=Galaxy s21
platform.version=12
device.udid=<your-device-udid>    # run: adb devices

# App — Option A: install APK each run
app.path=src/test/resources/apps/fleet_management.apk

# App — Option B: launch already-installed app
# app.package=com.example.fleet_management
# app.activity=com.example.fleet_management.MainActivity

# Behaviour
auto.grant.permissions=true
no.reset=false
full.reset=false
new.command.timeout=60
```

> All properties can be overridden via Maven system properties, e.g. `-Ddevice.udid=<udid>`

---

## Running Tests Locally

**1. Start Appium server**
```bash
appium --port 4723
```

**2. Connect your Android device**
```bash
adb devices   # note your device UDID
```

**3. Run the test suite**
```bash
mvn clean test
```

**4. Run with overrides**
```bash
mvn clean test \
  -Ddevice.udid=<udid> \
  -Ddevice.name="Galaxy S21" \
  -Dplatform.version=12 \
  -Dappium.server.url=http://127.0.0.1:4723
```

**5. Generate Allure report locally**
```bash
mvn allure:report
# Report generated at: target/site/allure-maven-plugin/index.html
```

---

## Parallel Execution

Tests run in parallel at the `<test>` level with 2 threads (configured in `testng.xml`):

```xml
<suite parallel="tests" thread-count="2">
```

Each test thread runs in an isolated JVM fork (`forkCount=2` in `pom.xml`), ensuring complete driver isolation between parallel test groups.

---

## Jenkins CI/CD Pipeline

The framework uses a **two-job pipeline**:

```
RR-Mobile-Builds  ──────►  RR-Mobile-Automation
  (Job 1)                       (Job 2)
  Auto-triggered                Manual trigger
  on git push                   pick a build from Job 1
  Compiles & packages           Runs tests against device
  Archives artifacts            Publishes reports + sends email
```

### Job 1 — RR-Mobile-Builds
- Triggered automatically on every push to `main`
- Compiles source with `mvn clean package -DskipTests`
- Archives compiled classes, dependencies, and resources

### Job 2 — RR-Mobile-Automation

**Parameters:**

| Parameter | Default | Description |
|-----------|---------|-------------|
| `BUILD_SELECTOR` | — | Build from Job 1 to test against |
| `DEVICE_UDID` | `192.168.0.188:41097` | ADB device UDID |
| `DEVICE_NAME` | `Galaxy s21` | Friendly device name |
| `PLATFORM_VERSION` | `12` | Android OS version |
| `APPIUM_PORT` | `4723` | Appium server port |
| `SUITE_FILE` | `testng.xml` | TestNG suite XML to run |
| `EMAIL_RECIPIENTS` | `hari.indora@faberwork.com` | Comma-separated email recipients |

**Pipeline stages:**
1. **Copy Build Artifacts** — copies pre-compiled artifacts from the selected Job 1 build
2. **Setup Appium Driver** — installs UIAutomator2 if not already installed
3. **Start Appium** — starts Appium server on the configured port
4. **Run Tests** — executes `mvn surefire:test` with device parameters
5. **Publish Reports** — publishes TestNG, JUnit, and Allure reports

**Build status based on pass rate:**
- 100% → `SUCCESS`
- ≥ 80% → `UNSTABLE`
- < 80% → `FAILURE`

---

## Reports

After each Jenkins run, the following reports are available from the build page:

| Report | URL |
|--------|-----|
| Allure Report | `<BUILD_URL>allure/` |
| TestNG Report | `<BUILD_URL>testReport/` |
| Build Console | `<BUILD_URL>console` |

An HTML email with the Allure report link, pass rate, and test summary is automatically sent to all configured recipients after every build.

---

## Jenkins Setup

### Required Plugins
- Copy Artifact
- Allure Jenkins Plugin
- TestNG Results Plugin
- Email Extension (emailext)
- Pipeline

### Required Tools (Manage Jenkins → Tools)
- JDK: `JDK21`
- Maven: `MAVEN3`
- Allure Commandline: `allure`

### Email (SMTP) Configuration
**Manage Jenkins → System → Extended E-mail Notification:**
- SMTP Server: `smtp.gmail.com`
- SMTP Port: `587`
- Use TLS: enabled
- Credentials: Gmail address + App Password

> Gmail App Passwords require 2-Step Verification to be enabled on the account.

### Script Approval
The pipeline uses the Jenkins Java API to resolve build numbers. A one-time approval is required:

**Manage Jenkins → In-process Script Approval** → approve:
```
staticMethod jenkins.model.Jenkins getInstance
```

---

## Key Design Decisions

- **ThreadLocal driver management** — `DriverManager` uses `ThreadLocal<AndroidDriver>` for safe parallel execution
- **Page Object Model** — all UI interactions encapsulated in `pages/` classes
- **No compile step in Job 2** — artifacts are copied from Job 1, ensuring tests always run against an exact known build
- **`catchError` in Run Tests stage** — test failures mark the build `UNSTABLE` so reports always publish even when tests fail
- **Allure SPI auto-registration** — `AllureTestNg` listener is registered via Service Provider Interface; do not add it manually to `testng.xml`