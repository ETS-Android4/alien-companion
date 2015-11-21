package com.gDyejeekis.aliencompanion.api.utils;

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
	"Bitcoin", "cars", "itookapicture", "beer", "frugalmalefashion", "TrollXChromosomes", "quityourbullshit", "GrandTheftAutoV",
	"investing", "NoFap", "Celebs", "whatisthisthing", "slowcooking", "Tinder", "bicycling", "outside", "thebutton", "pcgaming",
	"IWantToLearn", "LadyBoners", "Entrepreneur", "keto", "trashy", "writing", "Guitar", "wallpaper", "comicbooks", "FanTheories",
	"xboxone", "firstworldproblems", "howto", "wikipedia", "progresspics", "creepyPMs", "tf2", "Foodforthought", "behindthegifs",
	"rickandmorty", "adventuretime", "fantasyfootball", "iphone", "community", "tipofmytongue", "AnimalsBeingBros", "motorcycles",
	"CityPorn", "baseball", "worldpolitics", "getdisciplined", "self", "recipes", "chemicalreactiongifs", "Meditation", "math",
	"howtonotgiveafuck", "netsec", "environment", "ArtisanVideos", "magicTCG", "gamedev", "iamverysmart", "web_design", "CFB",
	"LucidDreaming", "Damnthatisinteresting", "TheLastAirbender", "Eyebleach", "montageparodies", "southpark", "futurama",
	"zelda", "Astronomy", "Diablo", "tumblr", "electronicmusic", "truegaming", "TopGear", "cosplay", "woodworking", "batman",
	"DeepIntoYouTube", "LetsNotMeet", "Justrolledintotheshop", "nostalgia", "Libertarian", "TheRedPill", "civ", "cosplaygirls",
	"GamePhysics", "carporn", "Guildwars2", "MMA", "roosterteeth", "shittyreactiongifs", "instant_regret", "IdiotsFightingThings",
	"ContagiousLaughter", "Physics", "Marvel", "Design", "nocontext", "fitmeals", "theydidthemath", "gardening", "socialskills",
	"gif", "MensRights", "WeAreTheMusicMakers", "depression", "rage", "KerbalSpaceProgram", "sysadmin", "nononono", "Homebrewing",
	"PenmanshipPorn", "WastedGifs", "HumanPorn", "FloridaMan", "PrettyGirls", "halo", "subredditoftheday", "TheSimpsons",
	"SandersForPresident", "unitedkingdom", "UnresolvedMysteries", "Frisson", "arresteddevelopment", "shittyfoodporn", "Health",
	"heroesofthestorm", "bodybuilding", "lego", "drunk", "boardgames", "dayz", "femalefashionadvice", "relationship_advice",
	"ArcherFX", "fatlogic", "oldpeoplefacebook", "Python", "lgbt", "confession", "Cinemagraphs", "electronic_cigarette",
	"asmr", "RoastMe", "Pareidolia", "buildapcsales", "Coffee", "britishproblems", "Metal", "calvinandhobbes", "techsupportgore",
	"nintendo", "fatpeoplestories", "AMA", "Christianity", "ZenHabits", "educationalgifs", "beards", "gamernews", "Awwducational",
	"australia", "teenagers", "3DS", "MilitaryPorn", "quotes", "skeptic", "somethingimade", "CasualConversation", "webdev",
	"ProgrammerHumor", "dogs", "CampingandHiking", "engineering", "jailbreak", "ANormalDayInRussia", "NoStupidQuestions",
	"DnD", "Glitch_in_the_Matrix", "Psychonaut", "OSHA", "justneckbeardthings", "CitiesSkylines", "DesignPorn", "ImaginaryLandscapes",
	"Demotivational", "startrek", "explainlikeIAmA", "beermoney", "ExpectationVsReality", "SquaredCircle", "hardbodies", "foodhacks",
	"redditgetsdrawn", "netflix", "financialindependence", "IASIP", "ColorizedHistory", "formula1", "lotr", "startups",
	"compsci", "happy", "WebGames", "productivity", "nyc", "whowouldwin", "blackpeoplegifs", "rpg", "DecidingToBeBetter",
	"coolguides", "cordcutters", "notinteresting", "dubstep", "Buddhism", "StartledCats", "answers", "casualiama", "HIMYM",
	"childfree", "masseffect", "DunderMifflin", "WhatsInThisThing", "ImaginaryMonsters", "RocketLeague", "blunderyears",
	"de_IAmA", "Shitty_Car_Mods", "AskCulinary", "shittyrobots", "battlefield_4", "Parenting", "MechanicalKeyboards",
	"xxfitness", "zombies", "ThingsCutInHalfPorn", "literature", "r4r", "Fantasy", "tldr", "friendsafari", "onetruegod",
	"dogecoin", "perfectloops", "PublicFreakout", "HighQualityGifs", "ffxiv", "classic4chan", "SubredditSimulator", "birdswitharms",
	"Paleo", "Bad_Cop_No_Donut", "corgi", "EDC", "witcher", "tech", "unexpectedjihad", "horror", "webcomics", "darksouls",
	"sweden", "wiiu", "TrueFilm", "reallifedoodles", "vinyl", "Cyberpunk", "PandR", "gamegrumps", "whitepeoplegifs", "betterCallSaul",
	"ClashOfClans", "summonerschool", "Paranormal", "EmmaWatson", "yoga", "Terraria", "raspberry_pi", "Marijuana", "Watches",
	"MealPrepSunday", "gainit", "hardware", "raisedbynarcissists", "weightroom", "google", "TrueAskReddit", "graphic_design",
	"diablo3", "MadeMeSmile", "Bundesliga", "InteriorDesign", "FancyFollicles", "wicked_edge", "babyelephantgifs", "hacking",
	"techsupport", "firefly", "OkCupid", "mashups", "dogpictures", "UniversityofReddit", "toosoon", "urbanexploration",
	"delusionalartists", "Seattle", "ShitRedditSays", "UNBGBBIIVCHIDCTIICBG", "Enhancement", "Military", "Baking", "chicago",
	"biology", "drawing", "javascript", "Heavymind", "jobs", "CombatFootage", "wowthissubexists", "RedditLaqueristas",
	"Survival", "simpleliving", "misleadingthumbnails", "Autos", "metalgearsolid", "DarkSouls2", "linguistics", "PS3",
	"bertstrips", "androidapps", "climbing", "Filmmakers", "Anxiety", "realasians", "skateboarding", "Sherlock",
	"elderscrollsonline", "finance", "ExposurePorn", "DCcomics", "StandUpComedy", "legaladvice", "AdrenalinePorn", "malelifestyle",
	"forwardsfromgrandma", "mylittlepony", "secretsanta", "ArchitecturePorn", "TinyHouses", "Graffiti", "Naruto", "AnimalsBeingDerps",
	"japan", "SocialEngineering", "coding", "Borderlands", "shittyadvice", "TrueDetective", "architecture", "Smite", "manga",
	"ireland", "GTAV", "backpacking", "toronto", "futureporn", "ketorecipes", "IndieGaming", "classicalmusic", "aviation",
	"DrunkOrAKid", "TheWayWeWere", "AntiJokes", "FifthWorldPics", "MoviePosterPorn", "RetroFuturism", "awesome", "MachinePorn"};

	
}
