#!/bin/zsh
# Deploy maven artefact in current directory into Maven central repository 
# using maven-release-plugin goals
source ~/.zshrc

read -p "Really deploy to maven cetral repository  (yes/no)? "

if ( [ "$REPLY" == "yes" ] ) then
  mvn release:clean release:prepare release:perform -B -e | tee maven-central-deploy.log
else
  echo 'Exit without deploy'
fi

