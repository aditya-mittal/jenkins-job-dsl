package com.dsl

@Grab('org.yaml:snakeyaml:1.23')
import org.yaml.snakeyaml.Yaml

Yaml parser = new Yaml()
def jobsConfigFile = readFileFromWorkspace("jenkins-jobs/src/main/groovy/com/dsl/jobconfigs.yml")

Object jobConfigs = parser.load(jobsConfigFile)

def jobsList = []

jobConfigs["jenkinsJobs"].each {
    def jobDetails = it

    String appName = it.name
    String remoteRepo = it.repo
    String repoBranch = it.branch
    String jenkinsFilePath = it.jenkinsFilePath

    boolean runImmediately = it.runImmediately

    println "config =>: AppName: $appName, Repo: $remoteRepo, Branch: $repoBranch, JenkinsFilePath: $jenkinsFilePath"

    String jobName = appName
    jobsList.push(jobName)

    def jobToBeCreated = pipelineJob(jobName) {
        triggers {
            scm('* * * * *')
        }

        concurrentBuild(false)

        definition {
            cpsScm {
                scm {
                    git {
                        remote {
                            url "${remoteRepo}"
                        }
                        branch "${repoBranch}"
                    }
                }
                scriptPath(jenkinsFilePath)
            }
        }
    }

    if (runImmediately) {
        queue(jobToBeCreated)
    }
}
