# Jenkins Shared Libraries

Reusable Jenkins pipeline steps for the [E-Commerce-App](https://github.com/Santosh-Pathak/E-Commerce-App) (EasyShop) CI/CD workflow. These Groovy functions live in `vars/` and can be called directly from any Jenkinsfile after the library is configured.

## Repository structure

```
jenkins-shared-libraries/
├── vars/
│   ├── clone.groovy              # Clone a Git repository
│   ├── docker_build.groovy       # Build Docker images
│   ├── docker_push.groovy        # Push images to Docker Hub
│   ├── update_k8s_manifests.groovy  # Update K8s manifests and push to Git
│   ├── run_tests.groovy          # Run unit tests
│   ├── trivy_scan.groovy         # Filesystem security scan with Trivy
│   ├── generate_reports.groovy   # Generate and archive build reports
│   ├── clean_ws.groovy           # Clean Jenkins workspace
│   └── hello.groovy              # Sample hello-world step
└── Readme.md
```

## Jenkins setup

1. In Jenkins, go to **Manage Jenkins → System → Global Pipeline Libraries**.
2. Add a library:
   - **Name:** `jenkins-shared-libraries` (or your preferred name)
   - **Default version:** `main`
   - **Retrieval method:** Modern SCM → Git
   - **Project repository:** URL of this repository
3. In your Jenkinsfile, load the library:

```groovy
@Library('jenkins-shared-libraries') _
```

Use the same library name you configured in step 2.

## Required credentials

Create these in **Manage Jenkins → Credentials**:

| Credential ID           | Type              | Used by              |
|-------------------------|-------------------|----------------------|
| `github-credentials`    | Username/password | `update_k8s_manifests` |
| `docker-hub-credentials`| Username/password | `docker_push`        |

## Available steps

### `clone`

Clones a Git repository at the given branch.

```groovy
clone('https://github.com/Santosh-Pathak/E-Commerce-App.git', 'main')
```

### `docker_build`

Builds a Docker image with optional Dockerfile and context.

```groovy
docker_build(
    imageName: 'santoshpathak7456/easyshop-app',
    imageTag: "${env.BUILD_NUMBER}",
    dockerfile: 'Dockerfile',
    context: '.'
)
```

| Parameter    | Required | Default      | Description              |
|--------------|----------|--------------|--------------------------|
| `imageName`  | Yes      | —            | Full image name          |
| `imageTag`   | No       | `latest`     | Image tag                |
| `dockerfile` | No       | `Dockerfile` | Path to Dockerfile       |
| `context`    | No       | `.`          | Build context directory  |

### `docker_push`

Logs in to Docker Hub and pushes the tagged image and `latest`.

```groovy
docker_push(
    imageName: 'santoshpathak7456/easyshop-app',
    imageTag: "${env.BUILD_NUMBER}",
    credentials: 'docker-hub-credentials'
)
```

| Parameter     | Required | Default                  | Description        |
|---------------|----------|--------------------------|--------------------|
| `imageName`   | Yes      | —                        | Full image name    |
| `imageTag`    | No       | `latest`                 | Image tag          |
| `credentials` | No       | `docker-hub-credentials` | Jenkins credential |

### `update_k8s_manifests`

Updates Kubernetes manifest image tags, ensures the ingress host is set, then commits and pushes changes to the app repository.

```groovy
update_k8s_manifests(
    imageTag: "${env.BUILD_NUMBER}",
    manifestsPath: 'kubernetes',
    gitCredentials: 'github-credentials',
    gitUserName: 'Jenkins CI',
    gitUserEmail: 'jenkins@example.com'
)
```

| Parameter        | Required | Default               | Description                          |
|------------------|----------|-----------------------|--------------------------------------|
| `imageTag`       | Yes      | —                     | Tag applied to deployment images     |
| `manifestsPath`  | No       | `kubernetes`          | Directory containing manifest YAML   |
| `gitCredentials` | No       | `github-credentials`  | GitHub credential ID                 |
| `gitUserName`    | No       | `Jenkins CI`          | Git commit author name               |
| `gitUserEmail`   | No       | `jenkins@example.com` | Git commit author email              |

**Manifests updated:**

- `08-easyshop-deployment.yaml` → `santoshpathak7456/easyshop-app:<tag>`
- `12-migration-job.yaml` → `santoshpathak7456/easyshop-migration:<tag>` (if present)
- `10-ingress.yaml` → host set to `easyshop.letsdeployit.com` (if present)

Changes are pushed to `https://github.com/Santosh-Pathak/E-Commerce-App`.

### `run_tests`

Placeholder for unit tests. Extend with your test command (for example `npm test` or `mvn test`).

```groovy
run_tests()
```

### `trivy_scan`

Runs a Trivy filesystem vulnerability scan on the workspace.

```groovy
trivy_scan()
```

Requires Trivy installed on the Jenkins agent.

### `generate_reports`

Writes a build report and archives it as a Jenkins artifact.

```groovy
generate_reports(
    projectName: 'EasyShop',
    imageName: 'santoshpathak7456/easyshop-app',
    imageTag: "${env.BUILD_NUMBER}"
)
```

### `clean_ws`

Cleans the Jenkins workspace.

```groovy
clean_ws()
```

## Example Jenkinsfile

```groovy
@Library('jenkins-shared-libraries') _

pipeline {
    agent any

    environment {
        IMAGE_NAME = 'santoshpathak7456/easyshop-app'
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Clone') {
            steps {
                clone('https://github.com/Santosh-Pathak/E-Commerce-App.git', 'main')
            }
        }

        stage('Test') {
            steps {
                run_tests()
            }
        }

        stage('Security Scan') {
            steps {
                trivy_scan()
            }
        }

        stage('Build') {
            steps {
                docker_build(imageName: IMAGE_NAME, imageTag: IMAGE_TAG)
            }
        }

        stage('Push') {
            steps {
                docker_push(imageName: IMAGE_NAME, imageTag: IMAGE_TAG)
            }
        }

        stage('Update K8s Manifests') {
            steps {
                update_k8s_manifests(imageTag: IMAGE_TAG)
            }
        }
    }

    post {
        always {
            generate_reports(
                projectName: 'EasyShop',
                imageName: IMAGE_NAME,
                imageTag: IMAGE_TAG
            )
            clean_ws()
        }
    }
}
```

## Project configuration

| Setting            | Value                                              |
|--------------------|----------------------------------------------------|
| GitHub repository  | `https://github.com/Santosh-Pathak/E-Commerce-App` |
| Docker Hub user    | `santoshpathak7456`                                |
| App image          | `santoshpathak7456/easyshop-app`                   |
| Migration image    | `santoshpathak7456/easyshop-migration`             |
| Ingress host       | `easyshop.letsdeployit.com`                        |

## License

Private project for CI/CD automation.
