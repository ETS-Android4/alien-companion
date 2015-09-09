package com.george.redditreader.api.utils;

public class RedditConstants {

	public static final int MAX_LIMIT_LISTING = 100;
	public static final int DEFAULT_LIMIT = 25;
	public static final int MAX_LIMIT_COMMENTS = 100;

	public static final int MAX_COMMENT_DEPTH = 4;
	
	/**
	 * Approximately the maximum listing size, including pagination until
	 * the end. This differs from request to request, but after some observations
	 * this is a nice upper bound.
	 */
	public static final int APPROXIMATE_MAX_LISTING_AMOUNT = 1300;
	
	public static final String[] defaultSubscribed = {"announcements","Art", "AskReddit", "askscience", "aww", "blog", "books", "creepy", "dataisbeautiful",
			"DIY", "Documentaries", "EarthPorn", "explainlikeimfive", "Fitness", "food", "funny", "Futurology", "gadgets", "gaming", "GetMotivated", "gifs",
			"history", "IAmA", "InternetIsBeautiful", "Jokes", "LifeProTips", "listentothis", "mildlyinteresting", "movies", "Music", "news", "nosleep", "nottheonion",
			"OldSchoolCool", "personalfinance", "philosophy", "photoshopbattles", "pics", "science", "Showerthoughts", "space", "sports", "television", "tifu", "todayilearned",
			"TwoXChromosomes", "UpliftingNews", "videos", "worldnews", "WritingPrompts"};

	public static final String[] popularSubreddits = {"all", "AskReddit", "funny", "pics", "todayilearned", "announcements", "worldnews", "science", "IAmA", "videos", "gaming", "movies", "Music", "aww", "news",
	"gifs", "askscience", "explainlikeimfive", "EarthPorn", "books", "television", "technology", "bestof", "LifeProTips", "WTF", "sports", "mildlyinteresting", "DIY", "AdviceAnimals", "Fitness", "Showerthoughts",
	"space", "tifu", "Jokes", "food", "InternetIsBeautiful", "photoshopbattles", "history", "gadgets", "nottheonion", "GetMotivated",
	"dataisbeautiful", "Futurology", "Documentaries", "personalfinance", "listentothis", "philosophy", "nosleep", "OldSchoolCool", "UpliftingNews",
	"Art", "creepy", "WritingPrompts", "TwoXChromosomes", "politics", "atheism", "woahdude", "trees", "leagueoflegends", "4chan",
	"Games", "programming", "sex", "Android", "fffffffuuuuuuuuuuuu", "gameofthrones", "reactiongifs", "cringepics", "interestingasfuck",
	"malefashionadvince", "Frugal", "YouShouldKnow", "HistoryPorn", "BlackPeopleTwitter", "pokemon", "pcmasterrace", "europe", "Minecraft",
	"lifehacks", "AskHistorians", "Unexpected", "comics", "tattoos", "JusticePorn", "nfl", "FoodPorn", "facepalm", "soccer",
	"wheredidthesodago", "oddlysatisfying", "cringe", "wallpapers", "relationships", "gentlemanboners", "TrueReddit", "freebies",
	"conspiracy", "GameDeals", "humor", "Cooking", "offbeat", "OutOfTheLoop", "hiphopheads", "buildapc", "nba", "anime", "skyrim",
	"geek", "StarWars", "loseit", "cats", "spaceporn", "hearthstone", "AbandonedPorn", "apple", "shittyaskscience", "talesfromtechsupport",
	"NetflixBestOf", "FiftyFifty", "RoomPorn", "GlobalOffensive", "firstworldanarchists", "baconreader", "me_irl", "circlejerk",
	"MakeUpAddiction", "EatCheapAndHealthy", "QuotesPorn", "DoesAnybodyElse", "photography", "mildlyinfuriating", "shutupandtakemymoney",
	"DotA2", "AnimalsBeingJerks", "wow", "Fallout", "doctorwho", "TumblrInAction", "MapPorn", "asoiaf", "scifi", "Steam", "thewalkingdead",
	"LearnUselessTalents", "dadjokes", "everymanshouldknow", "Economics", "breakingbad", "thatHappened", "guns", "DepthHub",
	"TalesFromRetail", "Whatcouldgowrong", "travel", "Drugs", "learnprogramming", "seduction", "hockey", "AskWomen", "nonononoyes",
	"PerfectTiming", "CrappyDesign", "fullmoviesonyoutube", "AskMen", "UnexpectedThugLife", "PS4", "changemyview", "harrypotter",
	"SubredditDrama", "DestinyTheGame", "holdmybeer", "business", "AlienBlue", "linux", "psychology", "offmychest", "canada",
	"smashbros", "youdontsurf", "SkincareAddiction", "MURICA", "starcraft", "BuyItForLife", "minimalism", "bodyweightfitness",
	"pettyrevenge", "youtubehaiku", "battlestations", "running", "standupshots", "CrazyIdeas", "polandball", "entertainment",
	"Bitcoin", "cars", "itookapicture", "beer", "frugalmalefashion", "TrollXChromosomes", "quityourbullshit", "GrandTheftAutoV"};
	
}
