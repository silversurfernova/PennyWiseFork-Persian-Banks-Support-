package com.pennywiseai.tracker.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.ChildFriendly
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Surfing
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The full set of icons a user can pick from for a custom category, keyed by a
 * stable string (persisted on [com.pennywiseai.tracker.data.database.entity.CategoryEntity.icon]
 * — never persist an ImageVector reference directly, keys must survive app updates).
 *
 * Every entry maps to a distinct icon — deliberately, since it's easy to add
 * two icons that read as "the same picture" to a user picking from a grid
 * (e.g. Flight + AirplanemodeActive are both just airplanes). Keep it that way
 * when adding more: check what's already here reads as visually different
 * before picking a new icon for a new concept.
 */
object CategoryIconSet {

    val icons: Map<String, ImageVector> = linkedMapOf(
        // Food & Drink
        "restaurant" to Icons.Default.Fastfood,
        "groceries" to Icons.Default.LocalGroceryStore,
        "coffee" to Icons.Default.LocalCafe,
        "bar" to Icons.Default.LocalBar,
        "cafe" to Icons.Default.Coffee,
        "cake" to Icons.Default.Cake,
        "ice_cream" to Icons.Default.Icecream,
        "bakery" to Icons.Default.BakeryDining,
        "wine" to Icons.Default.WineBar,
        "kitchen" to Icons.Default.Kitchen,

        // Transport
        "car" to Icons.Default.DirectionsCar,
        "bus" to Icons.Default.DirectionsBus,
        "bike" to Icons.Default.DirectionsBike,
        "commute" to Icons.Default.Commute,
        "fuel" to Icons.Default.LocalGasStation,
        "parking" to Icons.Default.LocalParking,
        "train" to Icons.Default.Train,
        "boat" to Icons.Default.DirectionsBoat,
        "taxi" to Icons.Default.LocalTaxi,

        // Travel
        "flight" to Icons.Default.Flight,
        "luggage" to Icons.Default.Luggage,
        "hotel" to Icons.Default.Hotel,
        "attractions" to Icons.Default.Attractions,
        "beach" to Icons.Default.BeachAccess,
        "globe" to Icons.Default.Public,

        // Shopping
        "shopping_bag" to Icons.Default.ShoppingBag,
        "shopping_cart" to Icons.Default.ShoppingCart,
        "store" to Icons.Default.Store,
        "diamond" to Icons.Default.Diamond,
        "gift" to Icons.Default.Redeem,
        "toys" to Icons.Default.Toys,

        // Money & Finance
        "receipt" to Icons.Default.Receipt,
        "payment" to Icons.Default.Payment,
        "payments" to Icons.Default.Payments,
        "credit_card" to Icons.Default.CreditCard,
        "wallet" to Icons.Default.AccountBalanceWallet,
        "bank" to Icons.Default.AccountBalance,
        "money" to Icons.Default.AttachMoney,
        "money_off" to Icons.Default.MoneyOff,
        "trending_up" to Icons.AutoMirrored.Filled.TrendingUp,
        "savings" to Icons.Default.Savings,
        "currency_exchange" to Icons.Default.CurrencyExchange,

        // Bills & Utilities
        "subscriptions" to Icons.Default.Subscriptions,
        "wifi" to Icons.Default.Wifi,
        "smartphone" to Icons.Default.Smartphone,
        "electricity" to Icons.Default.ElectricBolt,
        "water" to Icons.Default.WaterDrop,
        "gas_utility" to Icons.Default.LocalFireDepartment,
        "print" to Icons.Default.Print,

        // Health
        "hospital" to Icons.Default.LocalHospital,
        "pharmacy" to Icons.Default.LocalPharmacy,
        "dental" to Icons.Default.Healing,
        "vision" to Icons.Default.Visibility,
        "mental_health" to Icons.Default.Psychology,
        "vaccine" to Icons.Default.Vaccines,
        "accessibility" to Icons.Default.Accessible,
        "fitness" to Icons.Default.FitnessCenter,

        // Sports
        "martial_arts" to Icons.Default.SportsMartialArts,
        "soccer" to Icons.Default.SportsSoccer,
        "golf" to Icons.Default.SportsGolf,
        "tennis" to Icons.Default.SportsTennis,
        "basketball" to Icons.Default.SportsBasketball,
        "surfing" to Icons.Default.Surfing,
        "spa" to Icons.Default.Spa,

        // Family
        "face" to Icons.Default.Face,
        "child_care" to Icons.Default.ChildCare,
        "stroller" to Icons.Default.ChildFriendly,
        "elderly" to Icons.Default.Elderly,

        // Education
        "school" to Icons.Default.School,
        "book" to Icons.Default.Book,
        "study" to Icons.Default.MenuBook,
        "science" to Icons.Default.Science,

        // Work & Business
        "work" to Icons.Default.Work,
        "business" to Icons.Default.Business,
        "handshake" to Icons.Default.Handshake,
        "legal" to Icons.Default.Gavel,
        "build" to Icons.Default.Build,
        "tools" to Icons.Default.Handyman,

        // Home
        "home" to Icons.Default.Home,
        "apartment" to Icons.Default.Apartment,
        "furniture" to Icons.Default.Chair,
        "cleaning" to Icons.Default.CleaningServices,
        "laundry" to Icons.Default.LocalLaundryService,
        "key" to Icons.Default.VpnKey,

        // Entertainment
        "movie" to Icons.Default.Movie,
        "theater" to Icons.Default.TheaterComedy,
        "music" to Icons.Default.MusicNote,
        "piano" to Icons.Default.Piano,
        "games" to Icons.Default.SportsEsports,
        "podcast" to Icons.Default.Mic,
        "celebration" to Icons.Default.Celebration,

        // Tech & Devices
        "computer" to Icons.Default.Computer,
        "headphones" to Icons.Default.Headphones,
        "camera" to Icons.Default.Camera,
        "video_call" to Icons.Default.VideoCall,

        // Nature & Outdoors
        "park" to Icons.Default.Park,
        "terrain" to Icons.Default.Terrain,
        "eco" to Icons.Default.Eco,
        "flowers" to Icons.Default.LocalFlorist,
        "camping" to Icons.Default.OutdoorGrill,

        // Misc
        "pets" to Icons.Default.Pets,
        "favorite" to Icons.Default.Favorite,
        "star" to Icons.Default.Star,
        "donation" to Icons.Default.VolunteerActivism,
        "flag" to Icons.Default.Flag,
        "shield" to Icons.Default.Shield,
        "security" to Icons.Default.Security,
        "category" to Icons.Default.Category
    )

    /** Generic fallback for an unrecognized/missing icon key. */
    val fallback: ImageVector = Icons.Default.Category
}
