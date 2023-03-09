FROM confluentinc/cp-kafka-connect-base:7.3.0

ENV CONNECT_PLUGIN_PATH="/usr/share/java,/usr/share/confluent-hub-components,/data/connect-jars"

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-datagen:0.6.0