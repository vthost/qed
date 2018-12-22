#!/bin/bash

DIR=`dirname $0`

mkdir -p /tmp/build_qed
pushd /tmp/build_qed

git clone https://github.com/Quetzal-RDF/quetzal
git checkout Jena_3.4.0
cd quetzal

pushd com.ibm.research.quetzal.core
ant download
popd

pushd rdfstore-checker
ant download
popd

mvn clean install -DskipTests

popd

cp -r /tmp/build_qed/quetzal/rdfstore-checker/jni $DIR/../jni

rm -rf /tmp/build_qed
