package com.westjet.kafka.sample;

import java.util.Map;

import org.apache.avro.generic.GenericRecord;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.westjet.tiess.avro.models.TAAccountingWVIPayments;

public class SampleProducer {
	private static final Logger LOG = LoggerFactory.getLogger(SampleProducer.class);
	
	private static final TAAccountingWVIPayments[] payments = new TAAccountingWVIPayments[] {
		TAAccountingWVIPayments.newBuilder()
			.setAgentCode("1234")
			.setCollection(0.0f)
			.setCommission(100.0f)
			.setDateOfIssue("20200210")
			.build()
		
	};

	public static void main(String... args) throws Exception {
		// Parse Client Configs
		final CompositeConfiguration configs = new CompositeConfiguration();
		configs.addConfigurationFirst(ConfigUtils.newFileConfig("client.properties"));
		configs.addConfigurationFirst(ConfigUtils.newFileConfig("producer.properties"));

		final Map<String, Object> producerConfigs = ConfigUtils.toMap(configs);
		
		final KafkaProducer<GenericRecord, TAAccountingWVIPayments> producer = new KafkaProducer<>(producerConfigs);
		try {
			for(TAAccountingWVIPayments p: payments) {
				producer.send(new ProducerRecord<>("sample-data", p));
			}
		} finally {
			producer.close();
		}
	}
}
