<?php
/*
 *  Free Room Finder
 *
 *  Copyright (C) 2013 Jonathan Gillett and Joseph Heron
 *  All rights reserved.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

require_once "inc/auth.php";
require_once "inc/db_interface.php";
require_once "inc/validate.php";
require_once "inc/verify.php";
require_once "inc/utility.php";

session_start();

/*
 * Display the rooms that fit the given post data
 * 
 * 1. If the user is not logged in or their session is invalid
 * 
 * 		a) Redirect the user to the login page
 *
 * 2. If the user has submitted data (post data)
 * 
 * 		a) validate the post data
 * 			i) return an error if the submitted data is invalid
 * 
 * 3. If data is invalid
 * 
 *		a) Display 'invalid query'
 *		b) Have a button to redirect to room_request.php
 *
 * 4. If data is valid
 * 
 * 		a) query the database with post data
 * 		b) Display the results in fancy way
 * 		c) have option to download results
 * 		d) link to statitics page
 *
 * 
 */

/* Connect to the database */
$mysqli_conn = new mysqli("localhost", $db_user, $db_pass, $db_name);
$mysqli_login = new mysqli("localhost", $db_user, $db_pass, $db_login);

/* check connection */
if (mysqli_connect_errno()) {
	printf("Connect failed: %s\n", mysqli_connect_error());
	exit();
}


/* 1. If the user is not logged in or their session is invalid */
if (verify_login_cookie($mysqli_login, $SESSION_KEY) === false
	&& (!isset($_SESSION['login'])
	|| verify_login_session($mysqli_login, $_SESSION['login'], $SESSION_KEY) === false))
{
	/* Redirect the user to the login page */
	header('Location: login.php');
}

/* User has a valid login cookie set / has logged into the site with valid account */
elseif (verify_login_cookie($mysqli_login, $SESSION_KEY)
		|| verify_login_session($mysqli_login, $_SESSION['login'], $SESSION_KEY))
{
	/* FIX, forgot to account for when user has login cookie set but there is no session
	 * data, have to retrieve username from cookie and then set the session data
	*/
	if (verify_login_cookie($mysqli_login, $SESSION_KEY))
	{
		/* Get the login cookie data */
		$login_cookie = htmlspecialchars($_COOKIE['login']);

		/* Get the username from login cookie data and set session info */
		$username = username_from_session($mysqli_login, $login_cookie, $SESSION_KEY);
		set_session_data($mysqli_login, $username, $SESSION_KEY);
	}
}

/* Invalid cookie or session data/etc.. */
else
{
	/* Redirect the user to the login page */
	header('Location: login.php');
}


/*
 * 2. If the user has submitted data (post data)
 *
 * 		a) validate the post data
 * 			i) return an error if the submitted data is invalid
 *
 * 		b) parse the post data and redirect to the results page
 */
if (isset($_POST['select_time'])
	&& isset($_POST['select_duration'])
	&& isset($_POST['select_date'])
	&& isset($_POST['select_campus'])
	&& isset($_POST['select_num_people']))
{
	/* 4. If data is valid
	 *
	 * 		a) query the database with post data
	 * 		b) Display the results in fancy way
	 * 		c) have option to download results
	 * 		d) link to statitics page
	 */
	$duration =  $_POST['select_duration'];
	$start_time = $_POST['select_time'];
	$end_time =  get_end_time($start_time, $duration);
	$date = $_POST['select_date'];
	$day_of_week = get_day_of_week($date);
	$semester = get_semester($date);
	$campus = $_POST['select_campus'];
	$num_people = $_POST['select_num_people'];
	
	/* Save the values in the SESSION */
	$_SESSION['starttime'] = $start_time;
	$_SESSION['endtime'] = $end_time;
	$_SESSION['date'] = $date;
	$_SESSION['num_people'] = $num_people;
	
	/* Get the available rooms */ 
	$available = get_room_open(	$mysqli_conn, $start_time, 
								$end_time, $day_of_week, 
								$semester, $campus);
}

/* Invalid post data display error message */
else
{
	/* Redirect the user to the request page */
	header('Location: room_request.php');
}


/* Display the rooms found */
include 'templates/header-user.php';
include 'templates/results.php';

/* Include the footer */
include 'templates/footer.php';
?>