#include<vector>
#include<iostream>
#include<fstream>
using namespace std;


int main(int argc, char* argv[]){
    //create outfile
    ofstream orders("./orders.csv");
    ofstream orderItems("./orderItems.csv");
    double maxTotal = 1250000;

    //add headers to csv
    string ordersHeader = "employee_id,sub_total,date_time";
    string orderItemsHeader = "product_id,order_id,item_quantity";

    orders << ordersHeader << "\n";
    orderItems << orderItemsHeader << "\n";

    orders.close();
    return 0;
}