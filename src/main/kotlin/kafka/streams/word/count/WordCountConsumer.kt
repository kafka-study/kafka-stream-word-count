import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.KStreamBuilder
import java.util.*

    fun main(args: Array<String>){

        val config = Properties()
        config[StreamsConfig.APPLICATION_ID_CONFIG] = "wordcount-application"
        config[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "127.0.0.1:9092"
        config[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        config[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
        config[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass


        val builder = KStreamBuilder()

        val texLines = builder.stream<String, String>("word-count-input")
        val wordCounts =texLines.mapValues { it -> it.toLowerCase() }
            .flatMapValues { it -> it.split(" ") }
            .selectKey { key, word -> word }
            .groupByKey()
            .count("Counts")

        wordCounts.to(Serdes.String(), Serdes.Long(), "word-count-output");

        val streams = KafkaStreams(builder, config)
        streams.start()

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(Thread(Runnable { streams.close() }))

        // Update:
        // print the topology every 10 seconds for learning purposes
        println(streams.toString())

    }

