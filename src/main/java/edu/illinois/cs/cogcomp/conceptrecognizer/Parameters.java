package edu.illinois.cs.cogcomp.conceptrecognizer;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Parameters 
{
	// parameters for bootstrapping mention extraction
	public static int TOPFEATURES = 2000;
	public static double FEATURE_TH=0.3;
	public static int DECREASE_TH=200;
	public static int TRAINATLEASTKFEATURES = 2;
	public static int TESTATLEASTKFEATURES = 2;	
	public static int NUM_TRAIN_ROUNDS = 30;
	public static double MATCHSCORE = 0.5;
	public static double TFIDF_TH=0.13;
	public static boolean TFIDF_prune=false;
	
	public static String TESTDATA = "testset";
	public static String TESTSET_META = "testset.meta";
	
	// parameters for clustering
	public static double MENTION_SIM_TH=0.5;
	public static double CLUSTER_SIM_TH=0.4;
	
	public static String OutputDir="output/";
	//public static final String TRAIN_TF_FILE="newdata9/allText.new.tf";
	//public static final String TRAIN_IDF_FILE="newdata9/allText.new.idf";
	//public static final String NETWORK_FILE=dataDir+"release/2011/networks/paper-citation-network.txt";
	//public static final String META_FILE=dataDir+"release/2011/acl-metadata.txt";
	
	public static String[] categories = {"Application","Technique"};
	
	// Seeds
	public static HashSet<String> techACLSeeds = new HashSet<String>(Arrays.asList("unigram:bootstrapping","unigram:model","unigram:approach","unigram:algorithm","unigram:models"));
	public static HashSet<String> techGenSeeds = new HashSet<String>(Arrays.asList("unigram:model","unigram:approach","unigram:algorithm","unigram:models"));
	public static HashSet<String> appACLSeeds = new HashSet<String>(Arrays.asList("bigram:sense#disambiguation","bigram:entity#recognition","unigram:wsd","bigram:question#answering","unigram:transliteration","unigram:translation","unigram:mt","unigram:qa","unigram:extraction"));
	public static HashSet<String> appGenSeeds = new HashSet<String>(Arrays.asList("unigram:application", "context:bigram:-2:application#in", "context:bigram:-2:application#including", "context:bigram:-2:applications#in", "context:bigram:-2:applications#including"));
	
	public static void main(String[] args)
	{
	}
}
