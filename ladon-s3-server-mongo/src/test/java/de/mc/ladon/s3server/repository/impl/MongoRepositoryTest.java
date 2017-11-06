package de.mc.ladon.s3server.repository.impl;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.util.UUID;

public class MongoRepositoryTest extends S3RepositoryTests {

    private static MongodForTestsFactory factory;
    private static MongoClient mongo;
    private static MongodExecutable mongodExecutable;

    @BeforeClass
    public static void setupMongoDatabaseForTests() throws Exception {

        int port = 12345;

        Command command = Command.MongoD;

        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(command)
                .defaultsWithLogger(command, LoggerFactory.getLogger(MongoRepositoryTest.class))
                .build();

        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .setParameter("notablescan", "true")
                .net(new Net(port, Network.localhostIsIPv6()))
                .build();

        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

        MongodExecutable mongodExecutable = null;
        mongodExecutable = runtime.prepare(mongodConfig);
        MongodProcess mongod = mongodExecutable.start();

        MongoRepositoryTest.mongo = new MongoClient("localhost", port);

        MongoRepositoryTest.mongodExecutable = mongodExecutable;

        MongoRepositoryTest.factory = factory;
    }

    @Before
    public void setup() throws Exception {
        MongoConfig mongoConfig = new MongoConfig();
        this.cut = new MongoRepository(mongoConfig.gridFsTemplate(), mongoConfig.mongoTemplate());
        ((MongoRepository) cut).setupIndex();
    }

    @AfterClass
    public static void teardown() throws Exception {
        mongodExecutable.stop();
    }


    static class MongoConfig extends AbstractMongoConfiguration {

        private final String db;

        public MongoConfig() {
            this.db = "test-" + UUID.randomUUID();
        }

        @Bean
        public GridFsTemplate gridFsTemplate() throws Exception {
            return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
        }

        @Override
        protected String getDatabaseName() {
            return db;
        }

        @Override
        public Mongo mongo() throws Exception {
            return MongoRepositoryTest.mongo;
        }
    }
}
