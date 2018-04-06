package main;

import classifiers.NaiveBayesClassifier;

public class SpamFilterMain {

	public static void main(String[] args) {
		// TODO main class
		NaiveBayesClassifier classifier = new NaiveBayesClassifier();
		classifier.learn();
		classifier.fit();
	}
}
