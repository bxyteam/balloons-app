
echo "Compiling..."
rm -rf target
rm -rf runable

mvn clean package

mvn clean install

echo "Creating runnable directory"
mkdir -p runnable runnable/lib runnable/classes

echo "Copying bindist/lib to runnable/lib"
cp -r target/bindist/lib runnable/lib

echo "Copying classes to runnable/classes"
cp -r target/classes runnable/classes

echo "Copying scripts to runnable"
cp -r target/classes/scripts/. runnable

echo "Done"
