#include<vector>
#include<iostream>
#include<fstream>
using namespace std;


int main(int argc, char* argv[]){
    //create outfiles
    ofstream addOns("./addOns.csv");
    ofstream categories("./categories.csv");

    //add headers to csv
    string addOnsHeader = "add_on_id, add_on_name, add_on_price, category_id";
    string categoriesHeader = "category_id, category_name";

    addOns << addOnsHeader << "\n";
    categories << categoriesHeader << "\n";

    //add addons
    addOns << 0 << ",Pearls," << 0.4 << "," << 0 << "\n";
    addOns << 1 << ",Honey Jelly," << 0.3 << "," << 0 << "\n";
    addOns << 2 << ",Pudding," << 0.4 << "," << 0 << "\n";
    addOns << 3 << ",Ice Cream," << 0.5 << "," << 0 << "\n";
    addOns << 4 << ",Coffee Jelly," << 0.6 << "," << 0 << "\n";
    addOns << 5 << ",Crystal Boba," << 0.5 << "," << 0 << "\n";

    //add categories
    categories << 0 << ",Drinks" << "\n";
    
    addOns.close();
    categories.close();

    return 0;
}