package com.fo0.google.promotion.crawler;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.log4j.Log4j2;

@SpringBootApplication
@EnableScheduling
@Log4j2
public class Main {

	@Autowired
	private PromotionCrawler crawler;

	@Autowired
	private MailService mail;

	@Value("${crawler.username}")
	private String receiver;

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	/**
	 * crawling schedule 1 per day, after you started
	 * 
	 * @param receiver
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	@Scheduled(fixedRate = (1000 * 60 * 60) * 24)
	private void startCrawler() throws Exception, UnsupportedEncodingException {

		if (crawler.isPromotionAvailable()) {
			log.info("Promotions available - sending mail to: {}", receiver);
			mail.sendSimpleMessage(receiver, "Google Promotion sind wieder Verfügbar!",
					"Hey, \n es gibt wieder Promotions. \n Schau doch mal auf https://home.google.com/promotions");
		} else {
			log.info("Promotions not available - skip sending mail to: {}", receiver);
		}
	}

	/**
	 * For Debugging Input! <br>
	 * Also need to implements ApplicationRunner to the Main
	 */
//	@Override
//	public void run(ApplicationArguments args) throws Exception {
//		log.info("Application started with command-line arguments: {}", Arrays.toString(args.getSourceArgs()));
//		log.info("NonOptionArgs: {}", args.getNonOptionArgs());
//		log.info("OptionNames: {}", args.getOptionNames());
//
//		for (String name : args.getOptionNames()) {
//			log.info("arg-" + name + "=" + args.getOptionValues(name));
//		}
//
//		boolean containsOption = args.containsOption("person.name");
//		log.info("Contains person.name: " + containsOption);
//	}
}
