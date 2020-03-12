package com.westjet.kafka.sample;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.westjet.tiess.avro.models.TAAccountingWVIPayments;

public class SampleConsumer {
	private static final Logger LOG = LoggerFactory.getLogger(SampleConsumer.class);

	public static void main(String... args) throws Exception {
		// Parse Client Configs
		final CompositeConfiguration configs = new CompositeConfiguration();
		configs.addConfigurationFirst(ConfigUtils.newFileConfig("client.properties"));
		configs.addConfigurationFirst(ConfigUtils.newFileConfig("consumer.properties"));

		final Map<String, Object> consumerConfigs = ConfigUtils.toMap(configs);
		final Consumer<GenericRecord, TAAccountingWVIPayments> consumer = new KafkaConsumer<>(consumerConfigs);
		consumer.subscribe(Arrays.asList("sample-data"));

		try {
			while (true) {
				LOG.debug("Polling...");
				ConsumerRecords<GenericRecord, TAAccountingWVIPayments> records = consumer.poll(Duration.ofMillis(1000));
				LOG.debug("Got {} records", records.count());
				
				for (ConsumerRecord<GenericRecord, TAAccountingWVIPayments> record : records) {
					LOG.info("offset = {}, key = {}, value = {}", record.offset(), record.key(), record.value());
				}
			}
		} finally {
			consumer.close();
		}
	}
}
