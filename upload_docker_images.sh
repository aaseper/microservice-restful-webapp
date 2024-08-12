#!/usr/bin/env bash
set -e

cat << "EOF"
  ______      _                        _    __  __                                   
 |  ____|    | |                      | |  |  \/  |                                  
 | |__   ___ | | ___  _ __   __ _ _ __| | _| \  / | __ _ _ __   __ _  __ _  ___ _ __ 
 |  __| / _ \| |/ _ \| '_ \ / _` | '__| |/ / |\/| |/ _` | '_ \ / _` |/ _` |/ _ \ '__|
 | |___| (_) | | (_) | |_) | (_| | |  |   <| |  | | (_| | | | | (_| | (_| |  __/ |   
 |______\___/|_|\___/| .__/ \__,_|_|  |_|\_\_|  |_|\__,_|_| |_|\__,_|\__, |\___|_|   
                     | |                                              __/ |          
                     |_|                                             |___/           

EOF

cat << EOF
Eolopark Manager
https://github.com/aaseper/microservice-restful-webapp

===================================================

EOF

SCRIPT_VERSION="2024.04.06"
echo "upload_docker_images.sh version $SCRIPT_VERSION"
echo ""

# Check if docker is installed, then print the version, otherwise exit
if [ -x "$(command -v docker)" ]; then
    echo "Docker is installed."
    docker --version
else
    echo "Error: Docker is not installed." >&2
    exit 1
fi
echo ""

read -p "Do you want to push the services docker images? (y/n): " choice
case "$choice" in
	[Yy]|[Yy][Ee][Ss])
		echo -e "Continuing..."
		;;
	*)
		echo -e "Closing..."
		exit 1
		;;
esac
echo ""

read -p "Which username do you have at hub.docker.com?: " username
read -p "Which version do you want to upload?: " version

docker login

docker push $username/geoservice:$version
docker push $username/windservice:$version
docker push $username/server:$version
docker push $username/planner:$version