#!/bin/bash
# Creates an executable shell script which contains and configures the EMT

echo "Creating the installation package simply takes the JAR from the omod dir."
echo "Run Maven if necessary"

# Creates the actual installation script
cat <<EOF > install.sh
#!/bin/bash

echo Installing/updating EMR Monitoring Tool
echo " "

rm -rf ./EmrMonitoringTool

# untar the embedded payload
match=\$(grep --text --line-number '^PAYLOAD:$' install.sh | cut -d ':' -f 1)
payload_start=\$((match + 1))
tail -n +\$payload_start install.sh | tar -zxf -

mv shell-backend EmrMonitoringTool

./EmrMonitoringTool/configure.sh
# rm install.sh
exit 0

PAYLOAD:
EOF
chmod +x install.sh

# 'steal' frontend jar from maven packaging of omod
cp omod/target/*.jar shell-backend/lib

# tar all relevant stuff and append it to installation script
tar -zcvf /tmp/emt.tar.gz --exclude='src' --exclude='.settings' --exclude='.classpath' --exclude='.gitignore' --exclude='.project' --exclude='.DS_Store' --exclude='install.sh' --exclude=`basename $0` --exclude='.git' --exclude='.*' shell-backend
cat /tmp/emt.tar.gz >> install.sh 
#rm /tmp/emt.tar.gz
rm shell-backend/lib/emtfrontend*.jar

#mv install.sh bin
