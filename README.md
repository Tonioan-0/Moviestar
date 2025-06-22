## **MovieStar 🎬 (Read Me + Documentation)**

Moviestar is a  modern platform for movies and TV series with a sleek, user-friendly interface and personalized viewing experiences.

Represents the main controller for managing the application's primary pages and scenes.  We'll explore its key components, from the overarching visual **style** inspired by Material Design to the intricate systems governing **user authentication**, **profile management**, and **content display**.

You'll discover how we've built a seamless user experience, including dynamic **home page** recommendations, intuitive **search** capabilities, and a robust **movie playback** system. We'll also delve into the custom **UI components** that bring our application to life, the comprehensive **account management** features, and the underlying **database** structure that ensures data integrity. Finally, we'll touch upon the **external libraries** and services that enhance our application's capabilities, such as secure credential management and integration with the TMDb API.

## Documentation

**🎨Style**

```
The main style of the application is managed in general.css, the idea was to create
a flat minimal effect inspired by Material Design with a dark theme, so that all interfaces
could integrate it. It is made up of a visual hierarchy that goes from the width of the 
objects (very-large-item to small-item) to their texts (very-large-text to small-text), 
General then has different codes like primary, warning and other textures.
It also has global designs for objects like scrollPane
```

### 🚪Login

**Access**

```
The Access class is responsible for managing the login,
handling user input, password visibility toggling, layout configurations, and managing
internet connectivity checks. 
```

**Register**

```
The Register class is responsible for creating a new account, it ensure that the password 
is strong and the email is compatible with the usual email format.
```

**Reset**

```
ResetController is a controller class responsible for managing the password reset 
functionality within the application. It provides users the ability to reset their password, 
along with various interactive elements.
```

**Animationutils**

```
Utility class for handling animations on JavaFX Nodes.
Provides methods to apply various animation effects such as sliding, fading,
shaking, and pulsing to JavaFX components.
```

### 👷‍♂️profile managment (Profile)

**BaseProfiIe**

```
Abstract base class designed for creating and editing profile pages .
 This class provides the basic functionality to initialize UI components, 
 validate user input, manage image selections, and handle save operations .
 Subclasses must implement specific abstract methods to define their behavior
 and customize functionality as needed.
```

**CreateProfiIe** 

```
The class is responsible for managing the creation of user profiles 
within the application. It extends BaseProfileController. 
It also handles navigation between different pages, 
such as returning to the home screen or profile list.
```

**IconSVG**
```
The IconSVG class contains profile icons and their management
```
**ModifyProfile** 

```
Class for editing a user profile. Extends the Base class and
provides the functionalityto update the user name and profile icon. 
Includes navigation to pages .

```

**ProfileView**

```
ProfileView handles the display and management of user profiles attached to an account. 
It allows the user to view existing profiles, add new ones, or edit an existing profile.
```

### 🏠Home

**🟪MainPages**

```
This class handles user interaction, navigation, loading of pages, and transition effects. 
It also manages loading animations, header navigation logic,
and switching between different application scenes such as film scenes, 
settings, and profile creation.  
```

**🧭Header**

```
The HeaderController class manages the visual and interactive behavior of the 
application's header section. 
It handles the navigation buttons, search functionality, and profile menu.
```

**HomePage** 

```
It dynamically sets and displays recommendation lists based on the user's profile.
This includes personalized content, new releases, series suggestions, and more. 
It uses the main components like carousel and scrollview
```

**Series / Movie Pages** 

```
This pages filter the contents in db with user tastes
```

**🔎Search** 

```
The SearchController class is responsible for managing the search functionality,
interacting with TMDbAPIManager to take movies or TV series.
```

### 🧩Components

**🫸BufferAnimation**

```
Buffer animation is the main animation of the program, it used in mainPagesController to 
switch by the children
```

**🎠Carousel**

```
The Carousel class represents a UI control that allows users to cycle through 
a list of items. Items can be navigated sequentially in both forward and 
backward directions, with optional wrap-around behavior. 
The carousel also supports transitions, with customizable animation durations, 
and can be controlled programmatically.
```

**🎞️ScrollView**

```
The ScrollView class represents a customizable scrollable container 
 that displays a list of items. It extends the Control class and provides 
styling and property configurations for enhanced user interface display.

```

**🟪PopupMenu**

```
The popupMenu provides a customizable popup menu component.
It supports single or multiple columns of menu items.
The menu can be dynamically populated with any  Node, and includes options for adding 
separators and controlling the display position relative to an anchor node.
This component is styled to integrate with the application
```

### 🎞️ Movie Management (Movie_view)

**FilmPlayer**

```
The FilmPlayer class represents a video player ispied to google drive player
It has volume control, speed control, seeking functionality, fullscreen mode features.
```

**FilmScene**

```
The FilmScene class creates the view for a film, displaying different objects 
depending on whether the selected content is a movie or a TV series.
```

**FilmCard**

```
It's the main method to visualize movie/series in the home and in the other pages, 
it consist in a image and his shadow created in the controller.
The object have the metadata on the bottom, and show title and plot when the cursor is hover
```

**WindowCard**

```
It's implemented to work in particular with carusel,
the card show the image with a banner with metadata and the tile 
```

### ⚙️Account Managment (settings)

**Account Setting**

```
The class is responsible for managing a user's account settings.
 It includes operations such as changing user details, updating passwords, deleting accounts,
  and displaying specific account information.
```

**DeletePopUp**

```
Component designed to confirm the deletion of an account or user profile.
```

**FavouriteSetting** 

```
This class is responsible for managing and displaying content marked as user favorites.
```

**HistorySetting** 

```
This class is responsible for managing the display of the history of content watched by
 the user.

```

**SettingsView** 

```
This class manages the navigation to different settings views, such as account settings,
history, privacy, user list and favorites. It also provides the functionality to return 
to the home screen and interact with external resources such as GitHub.
```

**UpdatePassword** 

```
class is logically responsible for updating the user's password in the application.
```

**WatchList** 

```
The class is responsible for managing and displaying user-added content within 
its specific catalog
```

### 🍯Database

**DataBaseManager**

```
The DataBaseManager class is responsible for managing the database connection. 
It provides the functionality to establish a connection to a SQLite database 
provided with the application. Use of this class assumes that the database file is 
contained in the application's resources at the intended location.
```

**AccountDao**

```
The AccountDao class provides data access functionality for managing accounts
 stored in a database . It includes methods
to perform account creation, read, update, and delete operations.
```

**ContentDao**

```
The ContentDao class provides functionalities for interacting with
content-related data in the database, as well as managing content caching
to optimize performance. It contains methods for retrieving, inserting,
and deleting content, and supports filtering, homepage data generation,
and user-specific queries like watchlists and history.
```

**UserDao**

```
The class is responsible for handling user-related operations in the database. 
It serves as a data access object (DAO) to handle user-related queries, 
including inserting, deleting, updating, and querying users and their related content, 
such as history, watchlist, and favorites.

```

### 📚Libraries

**CredentialCryptManager**

```
Utility class for securely managing password hashing and verification using BCrypt.
This class provides methods to hash plaintext passwords and verify hashed passwords
against plaintext input, ensuring the integrity and security of password handling.
```

**EmailService**

```
The EmailService class is responsible for sending emails using a predefined sender email
and configured SMTP properties. It is designed for sending a password reset verification
code to a recipient email address.
```

**TMDbApiManager**

```
The TMDbApiManager class handles interactions with The Movie Database (TMDb) API.
Provides methods to make both synchronous and asynchronous requests to various 
TMDb API endpoints.Also includes functionality to manage content-related data and 
handle caching with a database.

```