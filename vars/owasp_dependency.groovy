def cal(){
  dependencyCheck AdditionalArguments: '--scan ./', odcInstallation:'OWASP'
  dependancyCheckPublisher pattern: '**/dependency-check-report.xml'
}
