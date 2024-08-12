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

SCRIPT_VERSION="2024.04.04"
echo "create_docker_images.sh version $SCRIPT_VERSION"
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

# Check that the docker daemon is running, otherwise run it
#if [ "$(systemctl is-active docker)" = "active" ]; then
#    echo "Docker daemon is running."
#else
#    echo "Docker daemon is not running. Starting it..."
#    sudo systemctl start docker
#    if [ "$(systemctl is-active docker)" = "active" ]; then
#        echo "Docker daemon started successfully."
#    else
#        echo "Error: Docker daemon could not be started." >&2
#        exit 1
#    fi
#fi
#echo ""


read -p "Do you want to build the services docker images? (y/n): " choice
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

read -p "Which username do you want to give the images?: " username
read -p "Which version do you want to give the images?: " version
echo "The images will be named as $username/geoservice:$version, $username/windservice:$version,
 $username/planner:$version and $username/server:$version"
echo ""

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "Location: $DIR"
echo ""

echo "Trying to build the GeoService image..."
cd geoservice
./mvnw spring-boot:build-image -Dimage-name=$username/geoservice:$version
if [ $? -eq 0 ]; then
    echo "GeoService image built successfully!"
else
    echo "There was a problem building the GeoService image."
    exit 1
fi
cd ..
echo ""

echo "Trying to build the WindService image..."
docker build -f windservice.Dockerfile -t $username/windservice:$version .
if [ $? -eq 0 ]; then
    echo "WindService image built successfully!"
else
    echo "There was a problem building the WindService image."
    exit 1
fi
echo ""

echo "Trying to build the Planner image..."
docker build -f planner.Dockerfile -t $username/planner:$version .
if [ $? -eq 0 ]; then
    echo "Planner image built successfully!"
else
    echo "There was a problem building the Planner image."
    exit 1
fi
echo ""

echo "Trying to build the Server image..."
cd server
./mvnw compile jib:dockerBuild -Dimage-name=$username/server:$version
if [ $? -eq 0 ]; then
    echo "Server image built successfully!"
else
    echo "There was a problem building the Server image."
    exit 1
fi
cd ..
echo ""

echo "All images built successfully!"
exit 0
