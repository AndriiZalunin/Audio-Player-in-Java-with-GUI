# Audio Player with GUI
## Overview
This application is designed to play music from user-created playlists
stored in a MySQL database. The JavaFX framework was used to create the user interface.

When the project is launched, a start window will appear displaying user playlists
(if they have already been created). To create a new playlist, click the `Create` button.
After selecting the desired playlist, you can start the player by clicking the `Select` button. 
You can also edit or delete it by clicking the corresponding buttons (`Edit` or `Delete`).

In the windows for creating and editing playlists, there are TreeView nodes that you can
interact with by double-clicking to select the next directory within
the one you are viewing, as well as a `Back` button to return to the previous folder.
To add to the playlist, select files that are no deeper than the open folder.

## Dependencies

- JDBC ( Java Database Connectivity) (<https://dev.mysql.com/downloads/connector/j/>);
    + Add it to your classpath;
    + Then add **mysql-connector-j-[version].jar** to the *External Libraries* by doing → (*File → Project Structure... → New Project Library*);
