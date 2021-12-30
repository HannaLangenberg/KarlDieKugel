# KarlDieKugel
A project from wintersemester 2020 using OpenGL and OpenCV.<br/>
It was built after a children's game. The idea is to navigate a ball through a labyrinth by tilting the board it is on.
Via OpenCV a colored pipe-cleaner was detected and its position and rotation within the camera frame tracked.
This was used to tilt a labyrinth which was modeled, loaded and controlled via OpenGL.

## Pictures
### Picture of the main Menu:
<img src="https://github.com/HannaLangenberg/KarlDieKugel/blob/main/resources/pictures/MainMenu.jpg" width="450">

### Picture of the labyrinth and how it is controlled:
<br/><img src="https://github.com/HannaLangenberg/KarlDieKugel/blob/main/resources/pictures/InGameLabyrinth.png" height="296">
<img src="https://github.com/HannaLangenberg/KarlDieKugel/blob/main/resources/pictures/InGameTracking.png" height="296">

## Videos to demonstrate how the program works:<br/>
### Video processing:
![Video processing demonstration](https://github.com/HannaLangenberg/KarlDieKugel/blob/main/resources/videos/C01BVVideo.mp4) → click on "View raw"
<br/>As you can see the pipe-cleaner is detected and controlls the labyrinth. In the console information about its position is displayed:
- "Nach oben" → up
- "Nach unten" → down
- "Nach rechts" → to the right
- "Nach links" → to the left
- "Halt" → stop

### Computer graphics:
![Computer graphics demonstration](https://github.com/HannaLangenberg/KarlDieKugel/blob/main/resources/videos/C01CGVideo.mp4) → click on "View raw"
<br/>As you can see every collision with the labyrinth's walls is detected and displayed in the console:
- "Does intersect" → only printed if we are currently intersecting
- "Hit" → the ball hit the hole in the ground and a new level can be loaded

But there is no reaction to the detected collisions. A position correction was implemented but disabled in the first part of the video.
<br/>The position correction is  shown in the second part of the video. It heavily glitches. This problem and its solution are both known but this was a very large project and there was no time to correctly implement a direction correction.
#### What was done
The position correction tried to place the ball back in front of the wall once a collision was detected, but the ball was of course still rolling towards the wall. So it was stuck in an endless cycle of rolling against the wall and being positioned in front of it again.
#### What should have been done
A physical approach would have been the way to go.
<br/>The main idea is to calculate a direction, velocity and acceleration vector from the balls center depending on the labyrinth's rotation and the ball's physical properties. Then the velocity vector has to be separated into the parallel and orthogonal components regarding the shock normal. Lastly invert the parallel component and combine the new parallel component with the original orthogonal component as the new velocity vector. In advance further aspects like elasticity, mass, ... could be considered.

<br/>_A basic structure and an OBJ loader were provided by our university._
