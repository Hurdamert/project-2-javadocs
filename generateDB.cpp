#include<vector>
#include<iostream>
#include<fstream>
#include<string>
#include<random>
#include<unordered_set>
using namespace std;


int main(int argc, char* argv[]){
    //create outfile
    ofstream orders("./orders.csv");
    ofstream orderItems("./orderItems.csv");
    double maxTotal = 1250000;

    //make vector of all items that can be sold
    vector<string> itemIndex = {"", "Classic Pearl Milk Tea", 
        "Honey Pearl Milk Tea", "Coffee Creama", "Coffee Milk Tea w/ Coffee Jelly",
         "Hokkaido Pearl Milk Tea", "Thai Pearl Milk Tea", "Taro Pearl Milk Tea", 
         "Mango Green Milk Tea", "Golden Retriever", "Coconut Pearl Milk Tea", 
         "Fresh Milk Tea", "Wintermelon w/ Fresh Milk", "Cocoa Lover w/ Fresh Milk", 
         "Matcha Fresh Milk", "Strawberry Matcha Fresh Milk", 
         "Mango Green Tea", "Passion Fruit Tea", "Berry Lychee Burst", 
         "Peach Tea w/ Honey Jelly", "Mango & Passion Fruit Tea", "Honey Lemonade", "Halo Halo", 
         "Wintermelon Lemonade", "Strawberry Coconut"};
    
    unordered_set<string, double> itemToPrice = {
        {"Classic Pearl Milk Tea", 5.80},
        {"Honey Pearl Milk Tea", 6.00},
        {"Coffee Creama", 6.50},
        {"Coffee Milk Tea w/ Coffee Jelly", 6.25},
        {"Hokkaido Pearl Milk Tea", 6.25},
        {"Thai Pearl Milk Tea", 6.25},
        {"Taro Pearl Milk Tea", 6.25},
        {"Mango Green Milk Tea", 6.50},
        {"Golden Retriever", 6.75},
        {"Coconut Pearl Milk Tea", 6.75},
        {"Fresh Milk Tea", 4.65},
        {"Wintermelon w/ Fresh Milk", 5.20},
        {"Cocoa Lover w/ Fresh Milk", 5.20},
        {"Matcha Fresh Milk", 6.25},
        {"Strawberry Matcha Fresh Milk", 6.50},
        {"Mango Green Tea", 5.80},
        {"Passion Fruit Tea", 6.25},
        {"Berry Lychee Burst", 6.25},
        {"Peach Tea w/ Honey Jelly", 6.25},
        {"Mango & Passion Fruit Tea", 6.25},
        {"Honey Lemonade", 5.20},
        {"Halo Halo", 6.95},
        {"Wintermelon Lemonade", 5.80},
        {"Strawberry Coconut", 6.50},
    };

    //make timestamp string
    int year = 2024;
    int month = 7;
    int day = 1;
    string timestamp = to_string(year) + "-" + to_string(month) + "-" + to_string(day);

    //add headers to csv
    string ordersHeader = "employee_id,sub_total,date_time";
    string orderItemsHeader = "product_id,order_id,item_quantity";

    orders << ordersHeader << "\n";
    orderItems << orderItemsHeader << "\n";

    long productID;
    long orderID = 1;
    long itemQuantity;
    int employeeID;
    double subTotal;

    int peakDaysLimit = 11000;
    int normalDaysLimit = 2700;

    //simulate 65 weeks
    for(int i = 0; i < 65; i++){
        //simulate 7 days
        for(int j = 0; j < 7; j++){
            
        }
    }

    orders.close();
    return 0;
}

string incrementDateTime(int year, int month, int day){

}