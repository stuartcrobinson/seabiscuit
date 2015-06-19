Seabiscuit.  Note:  It has been a month since working on this, so this document probably contains mistakes.  This was written to give a general description, purely for the purposes of job applications, and not as an accurate or professional readme/instructions.

----------------------------------------------------------------------------------------------
The long-term purpose of this project is to essentially be a fully automated hedge fund.  The goal was to see if there are any significant stock market opportunities for relatively small initial investments.

The current capability of this project is to download lots of stock data, analyze it, and then screen the stocks (and their future performances) using those many metrics, to look for situations where stock prices are prone to follow predictable movements.

I'm most excited about this version's custom relational in-memory storage and processing optimization.  This is the third version of the project, started in 2013.  This version was started from scratch in Jan 2015. 
Earlier versions were more "proof-of-concept" in nature, and very slow (took 17 hours to do what this version does in a few minutes - due to the amount of internal comparisons.  previous versions did too many read/write operations)
Check out the main brains and storage backbone in "supers" dir, namely the superclass Var.  
See the implementation in objects.finance.X and objects.prices.X

I'm also thrilled about how reflection allowed a huge simplification in variable-handling.  In earlier versions, when i wanted to change a metric, I had to change the code in 3 or 4 other places, which led to very tedious maintenance, and brittle code.
	

----------------------------------------------------------------------------------------------
"How to use me" is farther below.
----------------------------------------------------------------------------------------------


This is the future final intended workflow:

1.  Download historic data overnight
2.  During the day, load the data and calculate a bunch of metrics.
3.  Right before the markets close, download current data, calculate those metrics for today, and add them to the total data set
4.  Determine which stocks are ripe for short term growth.
5.  Purchase them using Interactive Brokers API.

--------------------------------------------------------------------------------------------------------------------------
The following capabilities are completed, each in isolation (one program does not work through the given steps autonomously):

I Download and Calculate

1.  Downloads historic data overnight.
2.  Loads the data and calculate a bunch of metrics.
3.  Quickly downloads current data, calculates those metrics for today, and adds them to the total data set

II Research

All data is now in memory

4.  In a loop, load a filter file where the user stipulates a bunch of stuff including which stocks to screen (where a given metric meets a given condition) and what dependent variables to calculate (ave price change over 2 days, 2 weeks, etc)
--------------------------------------------------------------------------------------------------------------------------

More detailed description of current function:

I DOWNLOAD HISTORIC DATA - uses Selenium or raw html (parsed with XPath or regex)

	First step is to download, overnight, lots of data that might be relevant to price movement.  Takes about 4 hours.
	The downloading mechanisms are in the "downloaders" dir.

	0.  Download list of US stocks (about 6000)
	1.  Download stock data into relevant objects (in "objects" dir).  
			Each of the following data packages are contained within the 9 subdirectories of the "objects" dir, named at the start of each following entry:
			- finance - Quarterly financial report data (past few years only) from morningstar.com
			- finance - Annual financial report data (past ten years) from morningstar.com
			- earnings - Earnings report data (past 15 years) from zachs.com, yahoo.com, and estimize.com
			- news - Relevant news articles (past ~1 year) from google.com and yahoo.com
			- people - Management information (current): executives' age, salary; number of board members, etc
			- profile - Corporate profile (current): num employees; stock's industry, sector (for yahoo and google (different)), and google-related-companies
			- sec - SEC report titles and dates (past 15 years)
			- short_interest - Short interest (past year)
			- splitsAndDividends - Splits and dividends (past 15 years)
			- prices - Daily price data (open, low, high, close) (past 15 years)
	2.  Download "macro" data = not stock-specific.
			- Weather in NY (currently the only one.  initially planned on adding national economic indicators later)
	
II ANALYZE

	1. Simple price metrics (weighted average over past n days, etc)
		For each day of downloaded historic price data, a bunch of metrics are calculated.  Earlier drafts of this program calculated over 100 metrics.  I calculated all the known "technical indicators" that I found referenced online.  Most were worthless.
	2. Stock category comparison metrics (CatAves)
		For many metrics, stock class (5 groups yahoo & google sector & industry; google-related companies) average metrics are calculated.  Then new metrics are created that compare a stock's metric with it's asset class average metric (ie CocaCola's PE minus the average PE of all stocks in the "Non-Cyclical Consumer Goods & Services" sector.
	3. Category Earnings Response
		This calculates stuff that might indicate how a stock price will change after an impending earnings release
		ex: the average price change of all the stocks immediately after their earnings release (if it's happened yet) in a given category within the current quarter.

III DOWNLOAD CURRENT DATA

	1.  Uses a yahoo API to fetch current prices for stocks.  
	
IV ANALYZE

	Data download and analysis is broken down in to two steps so that purchasing calculations can be done as quickly as possible following current data acquisition.
	
	
FILTER/SCREEN

	Now we have a huge in-memory database of historical stock prices and potentially relevant metrics.
	The Filter/Screen step allows you to query the data by modifying a filter file containing numerous metric restrictions.  So you can determine things like:
		"for stocks with PE between n and m -- what is their average price change over different time periods?  how would my initial monetary investment change if i invested in stocks with this characteristic, assuming i started with a certain principle and held a certain max number of stocks, and invested a certain max amount in each?"

----------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------

HOW TO USE ME

1.  In an IDE, open all "Agent" files in downloaders/downloadmaster
2.  Run Agent_0.  Wait about 10 seconds for it to download the stocks list, and then kick off all the other agents, in order.
3.  The last one won't download weather data unless you modify this line in G.java: System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Java My Libraries\\Selenium\\chromedriver.exe");
	It should be fine if you don't.  Weather won't download, which should be fine.
4.  Let it run for a bit.  A few or 20 minutes should get enough data for this to work barely
5.  in "main" dir open Master.java
6.  above the line containing "data.load(From.RAW);" are a number of ways to restrict the amount of loaded data (so it loads faster)
7.  run Master.java
8.  open the filter file, probably here: C:\stocks\Analysis\filter.pl (i used pl filetype just so notepad++ would format the syntax nicely)
9.  type some gibberish on a blank line
10. Press enter in the Master.java input window.  It should screw up, and direct you to the template filter file.  the header inputs on this file are not up-to-date, but it should give an accurate list of all the variables/metrics that are available for screening.
11. Delete the gibberish, press enter in Master.java, and you should get output with all zeros because the restrictions were too tight.
12. Delete everything between the first blank line and the "close $ >= 1" line.  Submit enter in Master.java display
13. You should get everything like this, below.  Below it, I'll explain the header comments and columns:

----------------------------------------------------------------------------------------------
EXAMPLE OUTPUT:

per exec: using 4 tickers, from AAN to AAP, startingdate: 20050101. subset offset, fraction: (null, null). using X's: [class objects.prices.X, class objects.finance.X, class objects.earnings.X].  excluding catTypes: [GOOGLE_INDUSTRY, GOOGLE_SECTOR, YAHOO_INDUSTRY, YAHOO_SECTOR]
per filt: R, rank1: 10 priceChPct_day increasing, close >= 1; 
prtfolio: [10, 2, 100000, 5, true, true, true, false] (maxStocks, perDay, init$, minHoldDays, sellASAP?, allowDD?, useFees?, onlySellOnValidSellDates?)...
                                               __ABS__             _______________DAYS________________                    ___________PORTFOLIO___________
  yVar     ave        n     ad   l-1 g0 g1 gA   g5 10     ave*n      ave      n     ad   l-1 g0 g1 gA   nCo  HpWk   span       %     min$    max$    fin$ ni  
pfolio                0    0.0                                                0                           0         2004     0.0   100000  100000       0  0  
pfolio    0.59      450    3.1   33  52 40 47   17  3       265     0.55    245    2.4   31  54 39 44     3         2005     9.9    93456  118935  109859 39  
pfolio    0.56      424    3.5   34  52 44 46   22  4       238     0.64    244    2.9   29  54 46 49     3         2006    12.1    99488  115940  112051 63  
pfolio    0.28      420    3.7   37  55 43 52   26  6       116     0.49    244    3.3   34  55 43 52     3         2007    -0.8    89505  114075   99243 60  
pfolio    0.67      418    5.8   40  52 45 47   44 16       282     0.64    242    5.2   40  53 46 48     3         2008     9.8    79528  121568  109830 69  
pfolio    0.46      403    4.2   39  51 43 47   33  6       187     0.34    244    3.6   41  50 41 44     3         2009     6.9    92184  117118  106851 76  
pfolio    1.07      433    2.8   22  65 50 49   15  3       465     1.09    243    2.4   21  69 48 48     3         2010    44.4    99518  145677  144400 50  
pfolio    0.74      451    3.9   38  56 45 48   26  7       334     0.72    243    3.4   36  54 48 48     3         2011    27.2    98221  130052  127202 35  
pfolio    0.10      444    3.2   40  50 37 48   18  5        44    -0.01    242    2.8   41  48 37 47     3         2012    -4.2    94168  109161   95782 37  
pfolio    1.13      457    2.6   24  62 47 46   11  2       514     1.11    246    2.2   24  67 50 47     4         2013    56.5   100000  156454  156454 35  
pfolio   -0.05      456    4.7   40  50 41 50   30 11       -21    -0.08    246    4.0   46  47 38 48     4         2014   -11.4    82564  120474   88648 36  
pfolio    1.16      158    4.2   38  54 46 45   24  8       183     1.08     82    3.4   34  56 51 49     4         2015    17.1    93283  120307  117089  6  
                                               __ABS__             _______________DAYS________________                    ___________PORTFOLIO___________
  yVar     ave        n     ad   l-1 g0 g1 gA   g5 10     ave*n      ave      n     ad   l-1 g0 g1 gA   nCo  HpWk   span       %     min$    max$    fin$ ni  
pfolio    0.54     4629    3.8   35  54 43 48   24  6      2510     0.54   2581    3.2   34  55 44 49     4     9    all   262.9    90283  427398  362902506  0.43s


----------------------------------------------------------------------------------------------
OUTPUT DESCRIPTIONS:



"per exec" describes to restriction parameters that stay the same each time Master.java is run.  constant through the ensuing loop
"per filt" describes restrictions that are valid only for these current results
"prtfolio" describes portfolio parameters.  the paramters are between brackets.  their descriptions follow, in parentheses.  

yVar - the dependent variable (or "y variable") (how much the price will change in the future).  meaningless during portfolio simulations so it just says pfolio
ave - ave price change
n - sample size
ad - average deviation.  i made this up to replace "standard deviation" (SD) because i noticed that SD has a heavy bias for differences greater than 1 (cos it squares stuff)
l-1 - the percentage of occasions where the price change was less than -1%
g0 - the percentage of occasions where the price change (yVar) was greater than 0
g1 - like g0, for g.t. 1%
gA - the percentage of occasions where the price change was greater than "ave"
ABS g5 -  the percentage of occasions where the absolute value of the price change was greater than 5.  I discovered that stocks that changed a lot since yesterday are likely to change a lot tomorrow, either up or down.  And there are options techniques that profit when a stock moves a lot in either direction
ABS g10 - "
ave*n - ave*n
DAYS group - these values are the same as the previous ones, except that they are comparisons between days instead of between specific stock events.  By "day," I mean the average of all the stock events on that day.  These are useful for spotting data that contains outliers. 
nCo - number of companies that were considered for this time period & yVar
HpWk - "hits per week"
PORTFOLIO group - these values are only calculated if "maxStocks" is set to be greater than 0 on the last line of the header rows in the filter file.  the first parameter
% the percent change of total money in the portfolio
min, max, final - the minimum, maximum, and final monetary values of the portfolio, throughout the simulation period
ni - number of invalid rows in the data.  (one row per stock per day).  these get thrown out.  several reasons for invalidating data, two of which are 1) 0-volume, and 2) a recent split (splits change a stock's price, so you have to do funny calculations to normalize historic data.  I noticed that Google and Yahoo both screw this up frequently, so i just throw these numbers out)
