-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 04, 2026 at 07:30 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `digitallibrary`
--

-- --------------------------------------------------------

--
-- Table structure for table `books`
--

CREATE TABLE `books` (
  `id` int(11) NOT NULL,
  `title` varchar(150) DEFAULT NULL,
  `author` varchar(120) DEFAULT NULL,
  `isbn` varchar(50) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `category` varchar(100) DEFAULT NULL,
  `publisher` varchar(150) DEFAULT NULL,
  `published_date` varchar(30) DEFAULT NULL,
  `stock` int(11) DEFAULT NULL,
  `pages` int(11) DEFAULT NULL,
  `copies` int(11) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `rating` decimal(3,2) DEFAULT 0.00,
  `cover_url` varchar(500) DEFAULT NULL,
  `is_new` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `books`
--

INSERT INTO `books` (`id`, `title`, `author`, `isbn`, `price`, `category`, `publisher`, `published_date`, `stock`, `pages`, `copies`, `description`, `created_at`, `rating`, `cover_url`, `is_new`) VALUES
(572, 'Harry', 'Poter', '726299182', 500.00, 'Horror', 'Sravani', '03/01/2026', 13, 250, NULL, 'comic book', '2026-01-03 10:21:41', 0.00, '\"C:\\xampp\\htdocs\\digitallibrary_API\\uploads\\mystic_quest.png\"', 1),
(573, 'Varun', 'barb', '8736368353592', 500.00, 'Horror', '0', '03/01/2021', 9, 200, NULL, 'Hemraj bemisaal Baliram Hemraj velta', '2026-01-03 11:07:19', 0.00, 'http://10.36.207.135/digitallibrary_API/uploads/book_1767438476_59991218.jpg', 1),
(574, 'The Mystic Quest', 'Alexandra Rivers', '9781234567890', 299.00, 'Fantasy', 'Fantasy Press', '2024-03-15', 10, 135, NULL, 'An epic fantasy adventure through enchanted forests and ancient magic. Follow the journey of a young mage as she discovers her hidden powers.', '2026-01-04 05:58:28', 4.50, 'uploads\\mystic_quest.png', 1),
(575, 'Code Master: Python for Beginners', 'Dev Johnson', '9782345678901', 499.00, 'Technology', 'TechBooks Publishing', '2024-01-20', 15, 157, NULL, 'The ultimate guide to learning Python programming from scratch. Includes 50+ hands-on projects and real-world examples.', '2026-01-04 05:58:28', 4.80, 'uploads\\code_master.png', 1),
(576, 'Silent Shadows', 'Rachel Black', '9783456789012', 249.00, 'Thriller', 'Dark Corner Books', '2023-11-10', 8, 97, NULL, 'A gripping crime thriller set in the dark streets of the city. Detective Maya Stone must solve mysterious disappearances before the killer strikes again.', '2026-01-04 05:58:28', 4.30, 'http://10.193.230.135/digitallibrary_API/uploads/book_1767507268_b5ec50b7.jpg', 1),
(577, 'Healthy Living: 30 Day Guide', 'Dr. Sarah Green', '9784567890123', 199.00, 'Health & Wellness', 'Wellness Publishing', '2024-02-01', 20, 124, NULL, 'Transform your lifestyle with this comprehensive 30-day wellness program. Includes meal plans, workout routines, and meditation techniques.', '2026-01-04 05:58:28', 4.60, 'http://10.193.230.135/digitallibrary_API/uploads/book_1767507277_e516855e.jpg', 1),
(578, 'Space Odyssey 2150', 'Marcus Chen', '9785678901234', 349.00, 'Science Fiction', 'Cosmic Books', '2024-04-05', 12, 128, NULL, 'Humanitys greatest adventure beyond the stars in the year 2150. When a mysterious signal is detected from a distant galaxy, Captain Elena Vance leads a crew of pioneers.', '2026-01-04 05:58:28', 4.70, 'http://10.193.230.135/digitallibrary_API/uploads/book_1767507357_ed23bd20.jpg', 1);

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`id`, `name`, `description`, `created_at`) VALUES
(1, 'Fiction', NULL, '2025-12-23 04:44:27'),
(2, 'Mythology', NULL, '2025-12-23 04:44:27'),
(3, 'History', NULL, '2025-12-23 04:44:27'),
(4, 'Non-Fiction', NULL, '2025-12-23 04:44:27');

-- --------------------------------------------------------

--
-- Table structure for table `fcm_tokens`
--

CREATE TABLE `fcm_tokens` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `token` varchar(255) NOT NULL,
  `device_info` varchar(255) DEFAULT NULL,
  `last_updated` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `fcm_tokens`
--

INSERT INTO `fcm_tokens` (`id`, `user_id`, `token`, `device_info`, `last_updated`) VALUES
(18, 17, 'fa6HiAkqR12AImFMuStDQg:APA91bGIeZUfDLH3pNY4SVgV2QawhYISLRHAk5YSRKLGD2lq25rgJ8SEGB_5a2Ag10aGQgjEkspwrehvxCt64qx2qvAROFxerPMqjxDj-cnhv8NunPyAzbM', 'I2202 14', '2026-01-04 06:13:32'),
(43, 33, 'f8796znXQCufB6dnsOb9ZK:APA91bFZFKjOwuWySlbBcjKAFbKRrWPJaRx4qOWMkLG0rgHp-N2-oCsOYBLujkNK6YpQ7CXLWbTTg5orPpJKgbkzkijplBCpRWPeBw9haTwT4Ef7mG9on_o', 'CPH2337 14', '2026-01-03 11:36:53');

-- --------------------------------------------------------

--
-- Table structure for table `invoices`
--

CREATE TABLE `invoices` (
  `id` int(11) NOT NULL,
  `invoice_id` varchar(50) NOT NULL,
  `user_id` int(11) NOT NULL,
  `book_id` int(11) DEFAULT NULL,
  `reason` varchar(100) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `status` enum('paid','unpaid','overdue') DEFAULT 'unpaid',
  `date` date DEFAULT curdate(),
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `libraries`
--

CREATE TABLE `libraries` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `address` text DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `working_hours` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `libraries`
--

INSERT INTO `libraries` (`id`, `name`, `address`, `contact_phone`, `working_hours`, `created_at`, `updated_at`) VALUES
(1, 'Central Library', '123 Main Street, City', '1234567890', 'Mon-Sat 9:00-18:00', '2025-12-05 09:44:16', '2025-12-05 09:44:16'),
(2, 'Central Library', '123 Main Street, City', '1234567890', 'Mon-Sat 9:00-18:00', '2025-12-06 07:52:27', '2025-12-06 07:52:27');

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `type` varchar(50) NOT NULL,
  `title` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`data`)),
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`id`, `user_id`, `type`, `title`, `message`, `data`, `is_read`, `created_at`) VALUES
(1, 1, 'general_announcement', '📢 Test', 'System working! 🎉', NULL, 0, '2026-01-02 10:50:54'),
(2, 33, 'general_announcement', '📢 Welcome!', 'Your notification system is working!', NULL, 1, '2026-01-02 11:13:20'),
(3, 33, 'general_announcement', '📢 Welcome!', 'Your notification system is working perfectly! 🎉', NULL, 1, '2026-01-02 11:17:11'),
(4, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'The Art of Mind\' has been confirmed! Pickup: 2026-01-04 9:00 AM - 11:00 AM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"10\",\"book_id\":\"304\",\"book_title\":\"The Art of Mind\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 1, '2026-01-02 11:37:29'),
(5, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'The Art of Science\' has been confirmed! Pickup: 2026-01-24 1:00 PM - 3:00 PM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"11\",\"book_id\":\"489\",\"book_title\":\"The Art of Science\",\"pickup_date\":\"2026-01-24\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 1, '2026-01-02 11:43:07'),
(6, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'Becoming\' has been confirmed! Pickup: 2026-01-02 9:00 AM - 11:00 AM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"12\",\"book_id\":\"545\",\"book_title\":\"Becoming\",\"pickup_date\":\"2026-01-02\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 1, '2026-01-02 11:47:58'),
(7, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'The Exorcist\' has been confirmed! Pickup: 2026-01-03 9:00 AM - 11:00 AM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"13\",\"book_id\":\"513\",\"book_title\":\"The Exorcist\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 1, '2026-01-03 03:31:38'),
(8, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'Pet Sematary\' has been confirmed! Pickup: 2026-01-03 11:00 AM - 1:00 PM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"14\",\"book_id\":\"514\",\"book_title\":\"Pet Sematary\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 1, '2026-01-03 03:32:57'),
(9, 33, 'reservation_approved', '✅ Reservation Approved', 'Your reservation for \'Pet Sematary\' has been approved! Please collect on time.', '{\"type\":\"reservation_approved\",\"reservation_id\":\"14\",\"book_title\":\"Pet Sematary\"}', 1, '2026-01-03 03:34:18'),
(10, 33, 'reservation_rejected', '❌ Reservation Rejected', 'Your reservation for \'Pet Sematary\' was rejected. Please contact library for details.', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"14\",\"book_title\":\"Pet Sematary\"}', 1, '2026-01-03 03:34:23'),
(11, 33, 'reservation_approved', '✅ Reservation Approved', 'Your reservation for \'The Exorcist\' has been approved! Please collect on time.', '{\"type\":\"reservation_approved\",\"reservation_id\":\"13\",\"book_title\":\"The Exorcist\"}', 1, '2026-01-03 03:34:42'),
(12, 33, 'reservation_approved', '✅ Reservation Approved', 'Your reservation for \'The Exorcist\' has been approved! Please collect on time.', '{\"type\":\"reservation_approved\",\"reservation_id\":\"13\",\"book_title\":\"The Exorcist\"}', 1, '2026-01-03 03:34:54'),
(13, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'Me Before You\' has been confirmed! Pickup: 2026-01-03 5:00 PM - 7:00 PM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"15\",\"book_id\":\"516\",\"book_title\":\"Me Before You\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 03:56:10'),
(14, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'Kafka on the Shore\' has been confirmed! Pickup: 2026-01-03 3:00 PM - 5:00 PM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"16\",\"book_id\":\"150\",\"book_title\":\"Kafka on the Shore\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 03:57:00'),
(15, 33, 'reservation_rejected', '❌ Reservation Rejected', 'Your reservation for \'Kafka on the Shore\' was rejected. Please contact library for details.', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"16\",\"book_title\":\"Kafka on the Shore\"}', 0, '2026-01-03 03:57:29'),
(16, 33, 'reservation_approved', '✅ Reservation Approved', 'Your reservation for \'Me Before You\' has been approved! Please collect on time.', '{\"type\":\"reservation_approved\",\"reservation_id\":\"15\",\"book_title\":\"Me Before You\"}', 0, '2026-01-03 03:57:39'),
(17, 33, 'reservation_approved', '✅ Reservation Approved', 'Your reservation for \'Becoming\' has been approved! Please collect on time.', '{\"type\":\"reservation_approved\",\"reservation_id\":\"12\",\"book_title\":\"Becoming\"}', 0, '2026-01-03 03:58:15'),
(18, 33, 'reservation_rejected', '❌ Reservation Rejected', 'Your reservation for \'The Art of Science\' was rejected. Please contact library for details.', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"11\",\"book_title\":\"The Art of Science\"}', 0, '2026-01-03 03:58:17'),
(19, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'Introduction to Success\' has been confirmed! Pickup: 2026-01-03 3:00 PM - 5:00 PM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:48'),
(20, 17, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(21, 16, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(22, 18, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(23, 19, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(24, 20, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(25, 21, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(26, 22, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(27, 23, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(28, 24, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(29, 25, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(30, 30, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(31, 31, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(32, 32, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(33, 34, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(34, 35, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(35, 36, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Success\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"17\",\"book_id\":\"239\",\"book_title\":\"Introduction to Success\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:12:49'),
(36, 33, 'reservation_approved', '✅ Reservation Approved', 'Your reservation for \'Introduction to Success\' has been approved! Please collect on time.', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(37, 16, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(38, 17, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(39, 18, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(40, 19, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(41, 20, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(42, 21, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(43, 22, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(44, 23, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(45, 24, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(46, 25, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(47, 30, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(48, 31, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(49, 32, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(50, 34, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(51, 35, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(52, 36, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'Introduction to Success\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"17\",\"user_name\":\"chanti\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 04:13:13'),
(53, 33, 'reservation_approved', '✅ Reservation Approved', 'Your reservation for \'The Art of Mind\' has been approved! Please collect on time.', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(54, 16, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(55, 17, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(56, 18, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(57, 19, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(58, 20, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(59, 21, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(60, 22, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(61, 23, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(62, 24, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(63, 25, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(64, 30, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(65, 31, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(66, 32, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(67, 34, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(68, 35, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(69, 36, 'reservation_approved', '📋 Reservation Approved', 'chanti\'s reservation for \'The Art of Mind\' was approved', '{\"type\":\"reservation_approved\",\"reservation_id\":\"10\",\"user_name\":\"chanti\",\"book_title\":\"The Art of Mind\"}', 0, '2026-01-03 04:13:20'),
(70, 33, 'reservation_rejected', '❌ Reservation Rejected', 'Your reservation for \'Frankenstein\' was rejected. Please contact library for details.', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(71, 16, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(72, 17, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(73, 18, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(74, 19, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(75, 20, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(76, 21, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(77, 22, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(78, 23, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(79, 24, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(80, 25, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(81, 30, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(82, 31, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(83, 32, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(84, 34, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(85, 35, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(86, 36, 'reservation_rejected', '📋 Reservation Rejected', 'chanti\'s reservation for \'Frankenstein\' was rejected', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"7\",\"user_name\":\"chanti\",\"book_title\":\"Frankenstein\"}', 0, '2026-01-03 04:13:23'),
(87, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'The 7 Habits of Highly Effective People\' has been confirmed! Pickup: 2026-01-03 1:00 PM - 3:00 PM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(88, 17, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 1, '2026-01-03 04:23:22'),
(89, 16, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(90, 18, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(91, 19, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(92, 20, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(93, 21, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(94, 22, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(95, 23, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(96, 24, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(97, 25, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(98, 30, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(99, 31, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(100, 32, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(101, 34, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(102, 35, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(103, 36, 'new_reservation', '📚 New Reservation', 'chanti reserved \'The 7 Habits of Highly Effective People\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"18\",\"book_id\":\"77\",\"book_title\":\"The 7 Habits of Highly Effective People\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:23:22'),
(104, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'Midnight\'s Children\' has been confirmed! Pickup: 2026-01-03 3:00 PM - 5:00 PM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(105, 17, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 1, '2026-01-03 04:25:09'),
(106, 16, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(107, 18, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(108, 19, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(109, 20, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(110, 21, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(111, 22, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(112, 23, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(113, 24, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(114, 25, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(115, 30, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(116, 31, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(117, 32, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(118, 34, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(119, 35, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(120, 36, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Midnight\'s Children\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"19\",\"book_id\":\"508\",\"book_title\":\"Midnight\'s Children\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"3:00 PM - 5:00 PM\"}', 0, '2026-01-03 04:25:09'),
(121, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'Secrets of Science\' has been confirmed! Pickup: 2026-01-03 11:00 AM - 1:00 PM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(122, 17, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(123, 16, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(124, 18, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(125, 19, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(126, 20, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(127, 21, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(128, 22, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(129, 23, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(130, 24, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(131, 25, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(132, 30, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(133, 31, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(134, 32, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(135, 34, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(136, 35, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(137, 36, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Secrets of Science\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"20\",\"book_id\":\"331\",\"book_title\":\"Secrets of Science\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:40:07'),
(138, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'Sapiens\' has been confirmed! Pickup: 2026-01-03 11:00 AM - 1:00 PM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:54'),
(139, 17, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55'),
(140, 16, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55'),
(141, 18, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55'),
(142, 19, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55'),
(143, 20, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55'),
(144, 21, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55'),
(145, 22, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55'),
(146, 23, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55'),
(147, 24, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55'),
(148, 25, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55'),
(149, 30, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55'),
(150, 31, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55'),
(151, 32, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55'),
(152, 34, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55');
INSERT INTO `notifications` (`id`, `user_id`, `type`, `title`, `message`, `data`, `is_read`, `created_at`) VALUES
(153, 35, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55'),
(154, 36, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Sapiens\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"21\",\"book_id\":\"59\",\"book_title\":\"Sapiens\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"11:00 AM - 1:00 PM\"}', 0, '2026-01-03 04:45:55'),
(155, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'Introduction to Art\' has been confirmed! Pickup: 2026-01-03 1:00 PM - 3:00 PM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(156, 17, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(157, 16, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(158, 18, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(159, 19, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(160, 20, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(161, 21, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(162, 22, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(163, 23, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(164, 24, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(165, 25, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(166, 30, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(167, 31, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(168, 32, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(169, 34, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(170, 35, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(171, 36, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Introduction to Art\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"22\",\"book_id\":\"483\",\"book_title\":\"Introduction to Art\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 04:56:08'),
(172, 33, 'new_book', '📚 New Book Added!', '\'Automatic Habits\' by James Clear is now available.', '{\"type\":\"new_book\",\"book_id\":\"570\",\"book_title\":\"Automatic Habits\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 07:18:57'),
(173, 8, 'new_book', '📚 New Book Added!', '\'Automatic Habits\' by James Clear is now available.', '{\"type\":\"new_book\",\"book_id\":\"570\",\"book_title\":\"Automatic Habits\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 07:18:57'),
(174, 9, 'new_book', '📚 New Book Added!', '\'Automatic Habits\' by James Clear is now available.', '{\"type\":\"new_book\",\"book_id\":\"570\",\"book_title\":\"Automatic Habits\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 07:18:57'),
(175, 10, 'new_book', '📚 New Book Added!', '\'Automatic Habits\' by James Clear is now available.', '{\"type\":\"new_book\",\"book_id\":\"570\",\"book_title\":\"Automatic Habits\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 07:18:57'),
(176, 11, 'new_book', '📚 New Book Added!', '\'Automatic Habits\' by James Clear is now available.', '{\"type\":\"new_book\",\"book_id\":\"570\",\"book_title\":\"Automatic Habits\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 07:18:57'),
(177, 12, 'new_book', '📚 New Book Added!', '\'Automatic Habits\' by James Clear is now available.', '{\"type\":\"new_book\",\"book_id\":\"570\",\"book_title\":\"Automatic Habits\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 07:18:57'),
(178, 13, 'new_book', '📚 New Book Added!', '\'Automatic Habits\' by James Clear is now available.', '{\"type\":\"new_book\",\"book_id\":\"570\",\"book_title\":\"Automatic Habits\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 07:18:57'),
(179, 14, 'new_book', '📚 New Book Added!', '\'Automatic Habits\' by James Clear is now available.', '{\"type\":\"new_book\",\"book_id\":\"570\",\"book_title\":\"Automatic Habits\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 07:18:57'),
(180, 15, 'new_book', '📚 New Book Added!', '\'Automatic Habits\' by James Clear is now available.', '{\"type\":\"new_book\",\"book_id\":\"570\",\"book_title\":\"Automatic Habits\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 07:18:57'),
(181, 26, 'new_book', '📚 New Book Added!', '\'Automatic Habits\' by James Clear is now available.', '{\"type\":\"new_book\",\"book_id\":\"570\",\"book_title\":\"Automatic Habits\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 07:18:57'),
(182, 27, 'new_book', '📚 New Book Added!', '\'Automatic Habits\' by James Clear is now available.', '{\"type\":\"new_book\",\"book_id\":\"570\",\"book_title\":\"Automatic Habits\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 07:18:57'),
(183, 28, 'new_book', '📚 New Book Added!', '\'Automatic Habits\' by James Clear is now available.', '{\"type\":\"new_book\",\"book_id\":\"570\",\"book_title\":\"Automatic Habits\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 07:18:57'),
(184, 29, 'new_book', '📚 New Book Added!', '\'Automatic Habits\' by James Clear is now available.', '{\"type\":\"new_book\",\"book_id\":\"570\",\"book_title\":\"Automatic Habits\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 07:18:57'),
(185, 33, 'reservation_approved', '✅ Reservation Approved', 'Your reservation for \'Introduction to Art\' has been approved! Please collect on time.', '{\"type\":\"reservation_approved\",\"reservation_id\":\"22\",\"book_title\":\"Introduction to Art\"}', 0, '2026-01-03 07:23:45'),
(186, 33, 'reservation_rejected', '❌ Reservation Rejected', 'Your reservation for \'Sapiens\' was rejected. Please contact library for details.', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"21\",\"book_title\":\"Sapiens\"}', 0, '2026-01-03 07:25:17'),
(187, 33, 'reservation_rejected', '❌ Reservation Rejected', 'Your reservation for \'Sapiens\' was rejected. Please contact library for details.', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"21\",\"book_title\":\"Sapiens\"}', 0, '2026-01-03 07:25:19'),
(188, 33, 'reservation_rejected', '❌ Reservation Rejected', 'Your reservation for \'Sapiens\' was rejected. Please contact library for details.', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"21\",\"book_title\":\"Sapiens\"}', 0, '2026-01-03 07:25:26'),
(189, 33, 'reservation_rejected', '❌ Reservation Rejected', 'Your reservation for \'Sapiens\' was rejected. Please contact library for details.', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"21\",\"book_title\":\"Sapiens\"}', 0, '2026-01-03 07:25:28'),
(190, 33, 'reservation_approved', '✅ Reservation Approved', 'Your reservation for \'Secrets of Science\' has been approved! Please collect on time.', '{\"type\":\"reservation_approved\",\"reservation_id\":\"20\",\"book_title\":\"Secrets of Science\"}', 0, '2026-01-03 07:38:53'),
(191, 33, 'reservation_approved', '✅ Reservation Approved', 'Your reservation for \'Midnight\'s Children\' has been approved! Please collect on time.', '{\"type\":\"reservation_approved\",\"reservation_id\":\"19\",\"book_title\":\"Midnight\'s Children\"}', 0, '2026-01-03 07:39:11'),
(192, 33, 'book_returned', '📚 Book Returned', 'Thank you for returning \'Introduction to Success\'. Your deposit will be refunded.', '{\"type\":\"book_returned\",\"reservation_id\":\"17\",\"book_title\":\"Introduction to Success\"}', 0, '2026-01-03 08:26:40'),
(193, 33, 'book_returned', '📚 Book Returned', 'Thank you for returning \'Understanding Adventure\'. Your deposit will be refunded.', '{\"type\":\"book_returned\",\"reservation_id\":\"1\",\"book_title\":\"Understanding Adventure\"}', 0, '2026-01-03 08:26:51'),
(194, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'Foundation\' has been confirmed! Pickup: 2026-01-03 1:00 PM - 3:00 PM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(195, 17, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(196, 16, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(197, 18, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(198, 19, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(199, 20, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(200, 21, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(201, 22, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(202, 23, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(203, 24, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(204, 25, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(205, 30, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(206, 31, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(207, 32, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(208, 34, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(209, 35, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(210, 36, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Foundation\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"23\",\"book_id\":\"532\",\"book_title\":\"Foundation\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"1:00 PM - 3:00 PM\"}', 0, '2026-01-03 09:00:35'),
(211, 33, 'book_returned', '📚 Book Returned', 'Thank you for returning \'Midnight\'s Children\'. Your deposit will be refunded.', '{\"type\":\"book_returned\",\"reservation_id\":\"19\",\"book_title\":\"Midnight\'s Children\"}', 0, '2026-01-03 09:15:06'),
(212, 33, 'new_book', '📚 New Book Added!', '\'Banda\' by taiyar is now available.', '{\"type\":\"new_book\",\"book_id\":\"571\",\"book_title\":\"Banda\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 09:49:10'),
(213, 8, 'new_book', '📚 New Book Added!', '\'Banda\' by taiyar is now available.', '{\"type\":\"new_book\",\"book_id\":\"571\",\"book_title\":\"Banda\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 09:49:10'),
(214, 9, 'new_book', '📚 New Book Added!', '\'Banda\' by taiyar is now available.', '{\"type\":\"new_book\",\"book_id\":\"571\",\"book_title\":\"Banda\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 09:49:10'),
(215, 10, 'new_book', '📚 New Book Added!', '\'Banda\' by taiyar is now available.', '{\"type\":\"new_book\",\"book_id\":\"571\",\"book_title\":\"Banda\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 09:49:10'),
(216, 11, 'new_book', '📚 New Book Added!', '\'Banda\' by taiyar is now available.', '{\"type\":\"new_book\",\"book_id\":\"571\",\"book_title\":\"Banda\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 09:49:10'),
(217, 12, 'new_book', '📚 New Book Added!', '\'Banda\' by taiyar is now available.', '{\"type\":\"new_book\",\"book_id\":\"571\",\"book_title\":\"Banda\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 09:49:10'),
(218, 13, 'new_book', '📚 New Book Added!', '\'Banda\' by taiyar is now available.', '{\"type\":\"new_book\",\"book_id\":\"571\",\"book_title\":\"Banda\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 09:49:10'),
(219, 14, 'new_book', '📚 New Book Added!', '\'Banda\' by taiyar is now available.', '{\"type\":\"new_book\",\"book_id\":\"571\",\"book_title\":\"Banda\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 09:49:10'),
(220, 15, 'new_book', '📚 New Book Added!', '\'Banda\' by taiyar is now available.', '{\"type\":\"new_book\",\"book_id\":\"571\",\"book_title\":\"Banda\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 09:49:10'),
(221, 26, 'new_book', '📚 New Book Added!', '\'Banda\' by taiyar is now available.', '{\"type\":\"new_book\",\"book_id\":\"571\",\"book_title\":\"Banda\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 09:49:10'),
(222, 27, 'new_book', '📚 New Book Added!', '\'Banda\' by taiyar is now available.', '{\"type\":\"new_book\",\"book_id\":\"571\",\"book_title\":\"Banda\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 09:49:10'),
(223, 28, 'new_book', '📚 New Book Added!', '\'Banda\' by taiyar is now available.', '{\"type\":\"new_book\",\"book_id\":\"571\",\"book_title\":\"Banda\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 09:49:10'),
(224, 29, 'new_book', '📚 New Book Added!', '\'Banda\' by taiyar is now available.', '{\"type\":\"new_book\",\"book_id\":\"571\",\"book_title\":\"Banda\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 09:49:10'),
(225, 33, 'new_book', '📚 New Book Added!', '\'Harry\' by Poter is now available.', '{\"type\":\"new_book\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 10:21:46'),
(226, 8, 'new_book', '📚 New Book Added!', '\'Harry\' by Poter is now available.', '{\"type\":\"new_book\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 10:21:46'),
(227, 9, 'new_book', '📚 New Book Added!', '\'Harry\' by Poter is now available.', '{\"type\":\"new_book\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 10:21:46'),
(228, 10, 'new_book', '📚 New Book Added!', '\'Harry\' by Poter is now available.', '{\"type\":\"new_book\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 10:21:46'),
(229, 11, 'new_book', '📚 New Book Added!', '\'Harry\' by Poter is now available.', '{\"type\":\"new_book\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 10:21:46'),
(230, 12, 'new_book', '📚 New Book Added!', '\'Harry\' by Poter is now available.', '{\"type\":\"new_book\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 10:21:46'),
(231, 13, 'new_book', '📚 New Book Added!', '\'Harry\' by Poter is now available.', '{\"type\":\"new_book\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 10:21:46'),
(232, 14, 'new_book', '📚 New Book Added!', '\'Harry\' by Poter is now available.', '{\"type\":\"new_book\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 10:21:46'),
(233, 15, 'new_book', '📚 New Book Added!', '\'Harry\' by Poter is now available.', '{\"type\":\"new_book\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 10:21:46'),
(234, 26, 'new_book', '📚 New Book Added!', '\'Harry\' by Poter is now available.', '{\"type\":\"new_book\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 10:21:46'),
(235, 27, 'new_book', '📚 New Book Added!', '\'Harry\' by Poter is now available.', '{\"type\":\"new_book\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 10:21:46'),
(236, 28, 'new_book', '📚 New Book Added!', '\'Harry\' by Poter is now available.', '{\"type\":\"new_book\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 10:21:46'),
(237, 29, 'new_book', '📚 New Book Added!', '\'Harry\' by Poter is now available.', '{\"type\":\"new_book\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 10:21:46'),
(238, 33, 'new_book', '📚 New Book Added!', '\'Varun\' by barb is now available.', '{\"type\":\"new_book\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 11:07:26'),
(239, 8, 'new_book', '📚 New Book Added!', '\'Varun\' by barb is now available.', '{\"type\":\"new_book\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 11:07:26'),
(240, 9, 'new_book', '📚 New Book Added!', '\'Varun\' by barb is now available.', '{\"type\":\"new_book\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 11:07:26'),
(241, 10, 'new_book', '📚 New Book Added!', '\'Varun\' by barb is now available.', '{\"type\":\"new_book\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 11:07:26'),
(242, 11, 'new_book', '📚 New Book Added!', '\'Varun\' by barb is now available.', '{\"type\":\"new_book\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 11:07:26'),
(243, 12, 'new_book', '📚 New Book Added!', '\'Varun\' by barb is now available.', '{\"type\":\"new_book\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 11:07:26'),
(244, 13, 'new_book', '📚 New Book Added!', '\'Varun\' by barb is now available.', '{\"type\":\"new_book\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 11:07:26'),
(245, 14, 'new_book', '📚 New Book Added!', '\'Varun\' by barb is now available.', '{\"type\":\"new_book\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 11:07:26'),
(246, 15, 'new_book', '📚 New Book Added!', '\'Varun\' by barb is now available.', '{\"type\":\"new_book\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 11:07:26'),
(247, 26, 'new_book', '📚 New Book Added!', '\'Varun\' by barb is now available.', '{\"type\":\"new_book\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 11:07:26'),
(248, 27, 'new_book', '📚 New Book Added!', '\'Varun\' by barb is now available.', '{\"type\":\"new_book\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 11:07:26'),
(249, 28, 'new_book', '📚 New Book Added!', '\'Varun\' by barb is now available.', '{\"type\":\"new_book\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 11:07:26'),
(250, 29, 'new_book', '📚 New Book Added!', '\'Varun\' by barb is now available.', '{\"type\":\"new_book\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"sender\":\"admin\",\"receiver\":\"all_users\"}', 0, '2026-01-03 11:07:26'),
(251, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'Harry\' has been confirmed! Pickup: 2026-01-03 5:00 PM - 7:00 PM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:29'),
(252, 17, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(253, 16, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(254, 18, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(255, 19, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(256, 20, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(257, 21, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(258, 22, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(259, 23, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(260, 24, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(261, 25, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(262, 30, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(263, 31, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(264, 32, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(265, 34, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(266, 35, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(267, 36, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"24\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:22:36'),
(268, 33, 'reservation_rejected', '❌ Reservation Rejected', 'Your reservation for \'Harry\' was rejected. Please contact library for details.', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"24\",\"book_title\":\"Harry\"}', 0, '2026-01-03 11:24:59'),
(269, 33, 'reservation_rejected', '❌ Reservation Rejected', 'Your reservation for \'Harry\' was rejected. Please contact library for details.', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"24\",\"book_title\":\"Harry\"}', 0, '2026-01-03 11:25:04'),
(270, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'Harry\' has been confirmed! Pickup: 2026-01-03 5:00 PM - 7:00 PM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:01'),
(271, 17, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(272, 16, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(273, 18, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(274, 19, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(275, 20, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(276, 21, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(277, 22, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(278, 23, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(279, 24, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(280, 25, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(281, 30, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(282, 31, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(283, 32, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(284, 34, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(285, 35, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(286, 36, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Harry\' for 2026-01-03', '{\"type\":\"new_reservation\",\"reservation_id\":\"25\",\"book_id\":\"572\",\"book_title\":\"Harry\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-03\",\"time_slot\":\"5:00 PM - 7:00 PM\"}', 0, '2026-01-03 11:26:03'),
(287, 33, 'reservation_rejected', '❌ Reservation Rejected', 'Your reservation for \'Harry\' was rejected. Please contact library for details.', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"25\",\"book_title\":\"Harry\"}', 0, '2026-01-03 11:26:25'),
(289, 33, 'general_announcement', 'Test Notification', 'This is a test push notification from debug script!', NULL, 0, '2026-01-03 11:35:53'),
(290, 33, 'general_announcement', 'Test Notification', 'This is a test push notification from debug script!', NULL, 0, '2026-01-03 11:36:06'),
(291, 33, 'general_announcement', 'Test Notification', 'This is a test push notification from debug script!', NULL, 0, '2026-01-03 11:36:09'),
(292, 33, 'reservation_confirmed', '✅ Reservation Confirmed', 'Your reservation for \'Varun\' has been confirmed! Pickup: 2026-01-04 9:00 AM - 11:00 AM', '{\"type\":\"reservation_confirmed\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:31'),
(293, 17, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(294, 16, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(295, 18, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(296, 19, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(297, 20, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(298, 21, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(299, 22, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(300, 23, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(301, 24, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(302, 25, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(303, 30, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(304, 31, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(305, 32, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(306, 34, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(307, 35, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(308, 36, 'new_reservation', '📚 New Reservation', 'chanti reserved \'Varun\' for 2026-01-04', '{\"type\":\"new_reservation\",\"reservation_id\":\"26\",\"book_id\":\"573\",\"book_title\":\"Varun\",\"user_id\":\"33\",\"user_name\":\"chanti\",\"pickup_date\":\"2026-01-04\",\"time_slot\":\"9:00 AM - 11:00 AM\"}', 0, '2026-01-03 11:38:33'),
(309, 33, 'reservation_rejected', '❌ Reservation Rejected', 'Your reservation for \'Varun\' was rejected. Please contact library for details.', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"26\",\"book_title\":\"Varun\"}', 0, '2026-01-03 11:40:13'),
(310, 33, 'reservation_rejected', '❌ Reservation Rejected', 'Your reservation for \'Varun\' was rejected. Please contact library for details.', '{\"type\":\"reservation_rejected\",\"reservation_id\":\"26\",\"book_title\":\"Varun\"}', 0, '2026-01-03 11:40:22');

-- --------------------------------------------------------

--
-- Table structure for table `password_resets`
--

CREATE TABLE `password_resets` (
  `id` int(11) NOT NULL,
  `email` varchar(255) NOT NULL,
  `otp` varchar(6) NOT NULL,
  `expiry` datetime NOT NULL,
  `used` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `password_resets`
--

INSERT INTO `password_resets` (`id`, `email`, `otp`, `expiry`, `used`, `created_at`) VALUES
(8, 'sravanireddy730@gmail.com', '688872', '2026-01-01 12:20:00', 1, '2026-01-01 06:20:00'),
(9, 'sravanireddy730@gmail.com', '752599', '2026-01-01 12:05:52', 0, '2026-01-01 06:20:52'),
(11, 'chantigorantla848@gmail.com', '261752', '2026-01-01 12:26:22', 0, '2026-01-01 06:26:22');

-- --------------------------------------------------------

--
-- Table structure for table `reservations`
--

CREATE TABLE `reservations` (
  `id` int(11) NOT NULL,
  `reservation_id` varchar(50) DEFAULT NULL,
  `book_name` varchar(200) DEFAULT NULL,
  `user_name` varchar(100) DEFAULT NULL,
  `user_email` varchar(100) DEFAULT NULL,
  `library_name` varchar(200) DEFAULT NULL,
  `date` varchar(30) DEFAULT NULL,
  `time_slot` varchar(50) DEFAULT NULL,
  `status` enum('Pending','Approved','Rejected') DEFAULT 'Pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `book_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `library` varchar(255) DEFAULT NULL,
  `pickup_date` varchar(255) DEFAULT NULL,
  `pickup_time` varchar(255) DEFAULT NULL,
  `library_id` int(11) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reservations`
--

INSERT INTO `reservations` (`id`, `reservation_id`, `book_name`, `user_name`, `user_email`, `library_name`, `date`, `time_slot`, `status`, `created_at`, `book_id`, `user_id`, `library`, `pickup_date`, `pickup_time`, `library_id`) VALUES
(1, NULL, NULL, NULL, NULL, NULL, NULL, '10:00 AM - 12:00 PM', '', '2025-12-31 07:16:39', 500, 33, NULL, '2026-01-03', NULL, 1),
(2, NULL, NULL, NULL, NULL, NULL, NULL, '10:00 AM - 12:00 PM', 'Rejected', '2025-12-31 07:16:56', 256, 33, NULL, '2026-01-03', NULL, 1),
(3, NULL, NULL, NULL, NULL, NULL, NULL, '10:00 AM - 12:00 PM', 'Rejected', '2025-12-31 07:24:41', 1, 33, NULL, '2026-01-03', NULL, 1),
(4, NULL, NULL, NULL, NULL, NULL, NULL, '10:00 AM - 12:00 PM', 'Approved', '2025-12-31 07:26:11', 257, 33, NULL, '2026-01-03', NULL, 1),
(5, NULL, NULL, NULL, NULL, NULL, NULL, '5:00 PM - 7:00 PM', 'Approved', '2026-01-01 07:41:47', 197, 33, NULL, '2026-01-01', NULL, 3),
(6, NULL, NULL, NULL, NULL, NULL, NULL, '5:00 PM - 7:00 PM', 'Approved', '2026-01-02 11:03:40', 513, 33, NULL, '2026-01-02', NULL, 3),
(7, NULL, NULL, NULL, NULL, NULL, NULL, '5:00 PM - 7:00 PM', 'Rejected', '2026-01-02 11:08:37', 512, 33, NULL, '2026-01-02', NULL, 3),
(8, NULL, NULL, NULL, NULL, NULL, NULL, '5:00 PM - 7:00 PM', 'Rejected', '2026-01-02 11:25:30', 514, 33, NULL, '2026-01-02', NULL, 3),
(9, NULL, NULL, NULL, NULL, NULL, NULL, '3:00 PM - 5:00 PM', 'Approved', '2026-01-02 11:30:47', 323, 33, NULL, '2026-01-02', NULL, 3),
(10, NULL, NULL, NULL, NULL, NULL, NULL, '9:00 AM - 11:00 AM', 'Approved', '2026-01-02 11:37:28', 304, 33, NULL, '2026-01-04', NULL, 3),
(11, NULL, NULL, NULL, NULL, NULL, NULL, '1:00 PM - 3:00 PM', 'Rejected', '2026-01-02 11:43:07', 489, 33, NULL, '2026-01-24', NULL, 3),
(12, NULL, NULL, NULL, NULL, NULL, NULL, '9:00 AM - 11:00 AM', 'Approved', '2026-01-02 11:47:58', 545, 33, NULL, '2026-01-02', NULL, 3),
(13, NULL, NULL, NULL, NULL, NULL, NULL, '9:00 AM - 11:00 AM', 'Approved', '2026-01-03 03:31:38', 513, 33, NULL, '2026-01-03', NULL, 3),
(14, NULL, NULL, NULL, NULL, NULL, NULL, '11:00 AM - 1:00 PM', 'Rejected', '2026-01-03 03:32:56', 514, 33, NULL, '2026-01-03', NULL, 3),
(15, NULL, NULL, NULL, NULL, NULL, NULL, '5:00 PM - 7:00 PM', 'Approved', '2026-01-03 03:56:08', 516, 33, NULL, '2026-01-03', NULL, 3),
(16, NULL, NULL, NULL, NULL, NULL, NULL, '3:00 PM - 5:00 PM', 'Rejected', '2026-01-03 03:56:59', 150, 33, NULL, '2026-01-03', NULL, 3),
(17, NULL, NULL, NULL, NULL, NULL, NULL, '3:00 PM - 5:00 PM', '', '2026-01-03 04:12:47', 239, 33, NULL, '2026-01-03', NULL, 3),
(18, NULL, NULL, NULL, NULL, NULL, NULL, '1:00 PM - 3:00 PM', 'Pending', '2026-01-03 04:23:21', 77, 33, NULL, '2026-01-03', NULL, 3),
(19, NULL, NULL, NULL, NULL, NULL, NULL, '3:00 PM - 5:00 PM', '', '2026-01-03 04:25:08', 508, 33, NULL, '2026-01-03', NULL, 3),
(20, NULL, NULL, NULL, NULL, NULL, NULL, '11:00 AM - 1:00 PM', 'Approved', '2026-01-03 04:40:06', 331, 33, NULL, '2026-01-03', NULL, 3),
(21, NULL, NULL, NULL, NULL, NULL, NULL, '11:00 AM - 1:00 PM', 'Rejected', '2026-01-03 04:45:54', 59, 33, NULL, '2026-01-03', NULL, 3),
(22, NULL, NULL, NULL, NULL, NULL, NULL, '1:00 PM - 3:00 PM', 'Approved', '2026-01-03 04:56:07', 483, 33, NULL, '2026-01-03', NULL, 3),
(23, NULL, NULL, NULL, NULL, NULL, NULL, '1:00 PM - 3:00 PM', 'Pending', '2026-01-03 09:00:34', 532, 33, NULL, '2026-01-03', NULL, 3),
(24, NULL, NULL, NULL, NULL, NULL, NULL, '5:00 PM - 7:00 PM', 'Rejected', '2026-01-03 11:22:23', 572, 33, NULL, '2026-01-03', NULL, 3),
(25, NULL, NULL, NULL, NULL, NULL, NULL, '5:00 PM - 7:00 PM', 'Rejected', '2026-01-03 11:25:49', 572, 33, NULL, '2026-01-03', NULL, 3),
(26, NULL, NULL, NULL, NULL, NULL, NULL, '9:00 AM - 11:00 AM', 'Rejected', '2026-01-03 11:38:27', 573, 33, NULL, '2026-01-04', NULL, 1);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `name` varchar(150) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `otp` varchar(10) DEFAULT NULL,
  `otp_expiry` datetime DEFAULT NULL,
  `user_type` varchar(20) NOT NULL DEFAULT 'reader',
  `library_name` varchar(255) DEFAULT NULL,
  `library_location` varchar(255) DEFAULT NULL,
  `type` enum('admin','user') DEFAULT 'user',
  `status` enum('active','blocked') DEFAULT 'active',
  `joined_date` date DEFAULT curdate()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `name`, `email`, `password`, `created_at`, `otp`, `otp_expiry`, `user_type`, `library_name`, `library_location`, `type`, `status`, `joined_date`) VALUES
(8, 'sravs', 'sravani123@gmail.com', 'welcome', '2025-12-22 10:10:34', NULL, NULL, 'reader', NULL, NULL, 'user', 'active', '2025-12-30'),
(9, 'Sravani Reddy', 'sravanireddy12@gmail.com', '$2y$10$I5JrBDR7IYsyCPTkliN5e.6AuczIVX/yOqRbKIZq/nhx9I9Mr.xf2', '2025-12-22 10:17:53', NULL, NULL, 'reader', NULL, NULL, 'user', 'active', '2025-12-30'),
(10, 'sravs', 'sravs12@gmail.com', '$2y$10$kc6leelieyMZjFCxWMg9yee/NOMrAxG/WXMcgJiF6uK5F9bwDAG1u', '2025-12-22 10:19:00', NULL, NULL, 'reader', NULL, NULL, 'user', 'active', '2025-12-30'),
(11, 'sravani', 'sravs123@gmail.com', '$2y$10$KPmBgt9nRdmg22D2tVSrRODUv1PJUqwzuPzz7HewgZQZKN25EFa1G', '2025-12-22 14:37:35', NULL, NULL, 'reader', NULL, NULL, 'user', 'active', '2025-12-30'),
(12, 'sravani', 'sravs1234@gmail.com', '$2y$10$S.mQpKy2ODiZGEglbyPLPeO2do9mCq/xDCjbuWUDdgmVI.hH00EVy', '2025-12-23 03:34:17', NULL, NULL, 'reader', NULL, NULL, 'user', 'active', '2025-12-30'),
(13, 'vivek', 'vivek123@gmail.com', '$2y$10$PB/zBEBqX3qn1kQ4apzDsOz/IPw2wxmmupwbDRYglersVBnT1arS2', '2025-12-23 03:39:49', NULL, NULL, 'reader', NULL, NULL, 'user', 'active', '2025-12-30'),
(14, 'teju', 'teju123@gmail.com', '$2y$10$vLazV/Sz8hcPomE9NDov7.X1Oxz7OoCJNip0HkPzavXmTXN/nqDCa', '2025-12-23 03:41:58', NULL, NULL, 'reader', NULL, NULL, 'user', 'active', '2025-12-30'),
(15, 'Raju', 'r@gmail.com', '$2y$10$KZTSZDuHBY98xERSt1uFlu3bTVobGC0vk04Ijb9blQEzrZCasDJGy', '2025-12-23 07:36:39', NULL, NULL, 'reader', NULL, NULL, 'user', 'active', '2025-12-30'),
(16, 'admin', 'admin@text.com', '$2y$10$CVKO6fEWrYiNal8zKBZnhOfJWvqoqFOehM3/WJU7DHNZvPVv110B6', '2025-12-23 07:38:38', NULL, NULL, 'admin', 'lib', 'chennai', 'user', 'active', '2025-12-30'),
(17, 'Sravani', 'sravanireddy730@gmail.com', '$2y$10$wZjK8JQaI2KAA2d78OwvQOqLtKc5czsRcaIIz0Bk1Z3SGKcFJrCje', '2025-12-23 08:08:30', NULL, NULL, 'admin', 'central library', 'Chennai', 'user', 'active', '2025-12-30'),
(18, 'Sravani Reddy', 'sravs567@gmail.com', '$2y$10$m3xXTk9VUK17ETsucchInORC7FTnBHovxO48e4ZJqSlJK9g3PA/oS', '2025-12-23 08:12:44', NULL, NULL, 'admin', 'central library', 'Chennai', 'user', 'active', '2025-12-30'),
(19, 'Sravani Reddy', 'sravanireddy098@gmail.com', '$2y$10$VOPdeTWAJ9O/Tq7jMmp2S.nUavKxDhm1dGwXTMWUNb23iuEOyRmDq', '2025-12-23 08:54:43', NULL, NULL, 'admin', 'central library', 'Chennai', 'user', 'active', '2025-12-30'),
(20, 'Sravani Reddy', 'sravanireddy698@gmail.com', '$2y$10$2EmfFqDfCKxG4K365F3g.OnmI6V5HOOSLP0pV1ieb6zGYKfCRdsA6', '2025-12-23 08:55:09', NULL, NULL, 'admin', 'central library', 'Chennai', 'user', 'active', '2025-12-30'),
(21, 'chanti', 'chanti123@gmail.com', '$2y$10$ZXjpJ8fJAnw.3E7Eq8gxLevux/uzXejWCsUDAQUBFiRhQ1wKGdFR2', '2025-12-23 08:57:32', NULL, NULL, 'admin', 'central library', 'Chennai', 'user', 'active', '2025-12-30'),
(22, 'teju', 'tejasri890@gmail.com', '$2y$10$yRrPJhQpBtJ2n8TpH8PCDe.3vS/alCI.y8YxHUE3lmwRjc8FM9cYK', '2025-12-23 09:05:49', NULL, NULL, 'admin', 'central library', 'banglore', 'user', 'active', '2025-12-30'),
(23, 'thosh', 'thosh123@gmail.com', '$2y$10$qQv4usLozZ3miJXf0kE2IuoLFfsL8mEzeOSaNB.ncnRGRa5Q1rkJ2', '2025-12-23 15:04:44', NULL, NULL, 'admin', 'central library', 'Chennai', 'user', 'active', '2025-12-30'),
(24, 'ammu', 'ammu@gmail.com', '$2y$10$JbnniigYHuQKthiJL8LjBua5s7gcdN93Lp7eLLo8SoTNEFKHgjHze', '2025-12-24 03:48:00', NULL, NULL, 'admin', 'central library', 'Chennai', 'user', 'active', '2025-12-30'),
(25, 'sravani', 'sravanireddy456@gmail.com', '$2y$10$/fLJpz/a.Er8P/TkzwCsqOcXEY3cOt2PJhfJu5dubV6PdJVwhoWzy', '2025-12-24 08:03:42', NULL, NULL, 'admin', 'central library', 'Chennai', 'user', 'active', '2025-12-30'),
(26, 'Admin', 'admin@gmail.com', '123', '2025-12-27 03:37:40', NULL, NULL, 'reader', NULL, NULL, 'admin', 'active', '2025-12-30'),
(27, 'John Doe', 'john@gmail.com', '123', '2025-12-27 03:37:40', NULL, NULL, 'reader', NULL, NULL, 'user', 'active', '2025-12-30'),
(28, 'Mary', 'mary@gmail.com', '123', '2025-12-27 03:37:40', NULL, NULL, 'reader', NULL, NULL, 'user', 'active', '2025-12-30'),
(29, 'Blocked User', 'block@gmail.com', '123', '2025-12-27 03:37:40', NULL, NULL, 'reader', NULL, NULL, 'user', 'blocked', '2025-12-30'),
(30, 'library', 'premkumart1087.sse@saveetha.com', '$2y$10$qDNJOXYm2BihGw7ovWirR.CkzAhm8oAkzqewNDZ06N4RV7yzIIi/G', '2025-12-29 11:48:56', NULL, NULL, 'admin', 'library', 'chennai', 'user', 'active', '2025-12-30'),
(31, 'central library', 'sravanireddy1121@gmail.com', '$2y$10$oGscNGqPOqo4R3YLWFSjxO4hEXnEL6h7TOS84hfspIXhtbudi3vZy', '2025-12-29 13:45:16', NULL, NULL, 'admin', 'central library', 'banglore', 'user', 'active', '2025-12-30'),
(32, 'central', 'sravanireddy123@gmail.com', '$2y$10$Eh1Yl4AlvnLhaCnG/RGXvOoB9qx.kj1TymKf6Rrs7sxk0ZzQ2ZCtu', '2025-12-29 13:46:26', NULL, NULL, 'admin', 'central', 'Chennai', 'user', 'active', '2025-12-30'),
(33, 'chanti', 'chantigorantla848@gmail.com', '$2y$10$5XONlpFC29C9qNh4q9Vc.u0AjKW5jLnqcstppT99pCymI5urJntLi', '2025-12-30 14:03:06', NULL, NULL, 'reader', NULL, NULL, 'user', 'active', '2025-12-30'),
(34, 'simats central library', 'thoshitha16@gmail.com', '$2y$10$UDf1gQynQSxGvGt7/hhUi.dhCiqlduAoWi2DKgLFOed5Wei5tRJ7O', '2025-12-31 13:39:56', NULL, NULL, 'admin', 'simats central library', 'Chennai', 'user', 'active', '2025-12-31'),
(35, 'simats central library', 'sirigajjala85@gmail.com', '$2y$10$nh8J6kQ/Ff92X8pR0UiU2OiL80/cjP5r19v0VU2S.m0Yp1Na7dqW2', '2026-01-01 06:27:56', NULL, NULL, 'admin', 'simats central library', 'Chennai', 'user', 'active', '2026-01-01'),
(36, 'simats central library', 'srividyapragada72@gmail.com', '$2y$10$mLleXFIo8USzCkvddUvp.uazzgYAoQLuv/hd7EGgeI4rapAqUiSW6', '2026-01-02 03:55:34', NULL, NULL, 'admin', 'simats central library', 'Chennai', 'user', 'active', '2026-01-02');

-- --------------------------------------------------------

--
-- Table structure for table `wishlist`
--

CREATE TABLE `wishlist` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `book_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `wishlist`
--

INSERT INTO `wishlist` (`id`, `user_id`, `book_id`, `created_at`) VALUES
(1, 33, 500, '2025-12-31 07:57:23');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `books`
--
ALTER TABLE `books`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `fcm_tokens`
--
ALTER TABLE `fcm_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `token` (`token`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_token` (`token`);

--
-- Indexes for table `invoices`
--
ALTER TABLE `invoices`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `libraries`
--
ALTER TABLE `libraries`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_created_at` (`created_at`);

--
-- Indexes for table `password_resets`
--
ALTER TABLE `password_resets`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `reservations`
--
ALTER TABLE `reservations`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `wishlist`
--
ALTER TABLE `wishlist`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `books`
--
ALTER TABLE `books`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=579;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `fcm_tokens`
--
ALTER TABLE `fcm_tokens`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=49;

--
-- AUTO_INCREMENT for table `invoices`
--
ALTER TABLE `invoices`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `libraries`
--
ALTER TABLE `libraries`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=311;

--
-- AUTO_INCREMENT for table `password_resets`
--
ALTER TABLE `password_resets`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `reservations`
--
ALTER TABLE `reservations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=37;

--
-- AUTO_INCREMENT for table `wishlist`
--
ALTER TABLE `wishlist`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `fcm_tokens`
--
ALTER TABLE `fcm_tokens`
  ADD CONSTRAINT `fcm_tokens_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `invoices`
--
ALTER TABLE `invoices`
  ADD CONSTRAINT `invoices_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
