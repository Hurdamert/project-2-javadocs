
#include<vector>
#include<iostream>
#include<fstream>
#include<string>
#include<random>
#include<unordered_map>
using namespace std;


//function to increment the date and time correctly (leap years not calculated)
string incrementDateTime(int& year, int& month, int& day){
    if(month == 12 && day == 31){
        year++;
        month = 1;
        day = 1;
    }
    else{
        if(month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12){
            if(day == 31){
                day = 1;
                month++;
            }
            else{
                day++;
            }
        }
        else if (month == 4 || month == 6 || month == 9 || month == 11){
            if(day == 30){
                day = 1;
                month++;
            }
            else{
                day++;
            }
        }
        else{
            if(day == 28){
                day = 1;
                month++;
            }
            else{
                day++;
            }
        }
        
    }

    //zero pad month
    string zeroPaddedMonth = "";
    if(month == 10 || month == 11 || month == 12){
        zeroPaddedMonth = to_string(month);
    }
    else{
        zeroPaddedMonth = "0" + to_string(month);
    }

    //zero pad day
    string zeroPaddedDay = "";
    if(day < 10){
        zeroPaddedDay = "0" + to_string(day);
    }
    else{
        zeroPaddedDay = to_string(day);
    }

    return to_string(year) + "-" + zeroPaddedMonth + "-" + zeroPaddedDay;
}

int main(int argc, char* argv[]){
    //create outfile
    ofstream orders("./orders.csv");
    ofstream orderItems("./orderItems.csv");

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
    
    unordered_map<string, double> itemToPrice = {
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
    string timestamp = to_string(year) + "-" + "0" + to_string(month) + "-" + "0" + to_string(day);

    //add headers to csv
    string ordersHeader = "employee_id,sub_total,date_time";
    string orderItemsHeader = "order_id,product_id,qty,item_price";

    orders << ordersHeader << "\n";
    orderItems << orderItemsHeader << "\n";

    long productID;
    long orderID = 1;
    long itemQuantity;
    int employeeID;
    double subTotal;
    double dayTotal;

    random_device rd;
    mt19937 gen(rd());

    int peakDaysLimit = 11000;
    int normalDaysLimit = 2700;

    //simulate 65 weeks
    for(int i = 0; i < 65; i++){
        //simulate 7 days
        for(int j = 0; j < 7; j++){
            dayTotal = 0;

            int currentLimit;

            if(month == 12 && day == 25 && year == 2024){
                currentLimit = peakDaysLimit;
            }
            else if(month == 8 && day == 21 && year == 2024){
                currentLimit = peakDaysLimit;
            }
            else if(month == 11 && day == 27 && year == 2024){
                currentLimit = peakDaysLimit;
            }
            else{
                currentLimit = normalDaysLimit;
            }

            while(dayTotal < currentLimit){
                //randomly choose between 1-3 drinks and 1-2 quantity per order
                subTotal = 0;
                int numDrinks = (gen() % 3) + 1;
                itemQuantity = (gen() % 2) + 1;

                for(int k = 0; k < numDrinks; k++){
                    //choose random drink and add price to subTotal
                    productID = (gen() % 24) + 1;
                    string drink = itemIndex[productID];
                    double price = itemToPrice.at(drink);

                    subTotal += price * itemQuantity;
                    
                    //add to orderItems
                    orderItems << orderID << "," << productID << "," << itemQuantity << ","
                        << price <<"\n";
                }
                
                employeeID = (gen() % 5) + 1;
                int randomHour = (gen() % 12) + 11;

                string timeWithHours = timestamp + " " + to_string(randomHour) + ":00:00";
                
                //add to orders
                orders << employeeID << "," << subTotal << "," << timeWithHours << "\n";

                orderID++;
                dayTotal += subTotal;
            }
            //change time
            timestamp = incrementDateTime(year, month, day);
        }
    }

    orders.close();
    orderItems.close();
    return 0;
}