#!/bin/bash

mkdir -p /tmp/build_qed
pushd /tmp/build_qed

git clone https://github.com/Quetzal-RDF/quetzal
cd quetzal

pushd com.ibm.research.quetzal.core
ant download
popd

pushd rdfstore-checker
ant download
popd

mvn clean install -DskipTests

popd
rm -rf /tmp/build_qed
