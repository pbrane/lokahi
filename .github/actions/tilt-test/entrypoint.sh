#!/bin/sh -l

apt update && apt install -y node openjdk-17-jdk
ctlptl create cluster kind --registry=ctlptl-registry

tilt ci
