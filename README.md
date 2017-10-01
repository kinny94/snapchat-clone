# Snap It - Snap Chat clone
Its an snapchat-alike android application developed only for education purposes. Its a clone of snapchat application in terms of functionalities. The application uses Firebase authentication for Signing up and loggin users. The images uploaded by the users are visibile to the person who has been added as the receiver of the image. The images are stored in the Firebase storage and appear on the feed of a particular user. Once the user has seen that image, the image is removed from the Firebase storage and you cannot view that image again. The user details like username are stored in the Firebase Realtime database.The images are extracted asynchronously with the help of dynamic imageviews created depending upon the number of images on the storage, for a particular database.

### Minimum SDK required - 5.0 or above

![Alt Text](/snapchat.gif)

The Application uses OOPS concepts like encapsulation and modularity for removing redundancy from the code. The app works within the complexity of O(n) with the use of proper datastructure like HashMaps. The application uses Internet permission to connect to the Firebase database. Clone the repository to play around. 
