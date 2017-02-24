 I) Problem Statement:
- Analyzing social networks on StackOverflow Web forums. People who post lots of questions vs. lots of answers.  As a starting point, they would like the following two lists of users:
	+ The top 10 users by total score on questions asked
	+ The top 10 users by total score on answers posted
	In both cases, they are only interested in posts from the past 6 months.
They are interested in usernames, not user ids.
- The data is from Stack Overflow (technically from their affiliate site Server Fault). The full, much-larger, data set here: https://archive.org/details/stackexchange. The files that you will need are stackoverflow.com-Posts.7z and stackoverflow.com-Users.7z That download is much too big to fit into an e-mail; it's 9.2GB compressed.

II)Solution: Split the big file of the posts and process it
 - 4 constants:
    public static int ID_RANGE = 700000;
	public static int NUMBER_OF_FILES = 10;
	public static String QUESTION_FILE_PATH = ".\\UserData\\QuestionUserData";
	public static String ANSWER_FILE_PATH = ".\\UserData\\AnswerUserData";
- The ID_RANGE and NUMBER_OF_FILE should change together to make sure it cover the range of User ID. With the medium file, ID_RANGE should be 70000 and NUMBER_OF_FILES = 10. In the short file, ID_RANGE should be 50 and NUMBER_OF_FILE should be 3 to see the split files. The QUESTION_FILE_PATH and ANSER_FILE_PATH are the names of split files and they will be appended with the index the the loop within NUMBER_OF_FILES.
- Samples of inputs for main method
For short files with no splitting:
"SplitedData\users-short.xml"
"SplitedData\posts-short.xml"
For short files with splitting of the posts file
"SplitedData\users-short.xml"
"SplitedData\posts-short1.xml"
"SplitedData\posts-short2.xml"
For medium files with splitting of the posts file
"SplitedData\users-medium.xml"
"SplitedData\posts-medium1.xml"
"SplitedData\posts-medium2.xml"
"SplitedData\posts-medium3.xml"
"SplitedData\posts-medium4.xml"
"SplitedData\posts-medium5.xml"
"SplitedData\posts-medium6.xml"
"SplitedData\posts-medium7.xml"
"SplitedData\posts-medium8.xml"

- The number of users is not too big so I don't split the user file, when we read the users file, all users will live in memory. It reduces the complexity of problem by not split user file. The posts file need to be split in case of processing huge posts file.
- The User and UserData class implement Comparable interface and UserData class also overrides method equals. This will helps us in searching and sorting data.
- The findUser method is changed in which binary searching is used. After we read users from file, we will sort it so the findUser method can use binary searching (This step can be skipped because i think the users file already has order). 
- After reading users and posts files, I get out the list of UserData ordered by question score and answer score and store these files and grouped them by UserID. To see the result of these intermediate files grouped by UserID, in the main method, we need to comment out the functions removeIntermidiateFiles. These intermediate files need to be deleted after each time we run the program to make sure it runs correctly for the next tries. The process of writing these intermediate files includes searching and merging UserData in order to count the score of the posts correctly for each user.
- The final step is reading top UserData from the intermediate files then merge them together. To merge these sorted list of UserData, I use the PriorityQueue with the size of number of TOP_USER to store all the first items from these intermediate files.(The first item is the item with highest score) .After that, I extract one by one from PriorityQueue to put into the return list of UserData. Each time I get out 1 item from the PriorityQueue, I will get next item from the list that contains the item in the Queue and put this next item back into the PriorityQueue. The process is repeated until the queue is empty. After that we will get out the first TOP_USER items to print out to the console.
- The Space Complexity of the program will be (the size of split post files  or intermediate files) + the size of user file + the size of the queue. The the main running time is reading user file + reading split post files + the time of reading and writing intermediate files + the time of merging grouped by id intermediate files+ log(size of user file) for searching user + the time of merging sorted list from TOP_USER of intermediate files.
