from faker import Faker
from datetime import datetime
import os, random

# Initialize Faker to generate fake data
fake = Faker()

business_id = os.getenv('business_id', 'business123')
product_id = os.getenv('product_id', 'product123')

now = datetime.now()
timestampInString = now.strftime("%d-%m-%Y-%H-%M-%S")

# Change the directory to your desired location
file_dir = os.getenv('FILE_OUTPUT_DIRECTORY', '/Users/srirajkadimisetty/sample-data/txt/')
file_name_middle_part = f'{os.getenv('FILE_NAME', fake.lexify(text='????'))}'

file_name = f"{business_id}_{product_id}_{file_name_middle_part}_{timestampInString}.txt"

# Define the number of records to generate
num_records = os.getenv('NUMBER_OF_FAKE_RECORDS', 10)

output_file = f"{file_dir}{file_name}_{str(num_records)}.txt"




def generate_data():
    # Define the number of columns in the dataset (minimum 13 columns)
    num_columns = 13

    # Define the header for the CSV file
    #header = 'id,name,phone,address,dob,ssn,occupation,income,risk_score,transaction_amount,country,email,status'
    header_line = f'BusinessName,{business_id},ProductName,{product_id},xxx,yyy'

    with open(output_file, mode='w') as file:
        # Write header row with commas
        file.write(header_line + '\n')

        for _ in range(num_records):
            row = [fake.random_int(1, 1000000), fake.name(), fake.phone_number(), fake.address(),
                   fake.date_of_birth(),
                   fake.ssn(),
                   fake.job(), random.randint(20000, 200000), random.randint(100, 900), random.randint(10, 10000),
                   fake.country(), fake.email(), random.choice(['approved', 'pending', 'rejected'])]

            # Write row with pipe delimiter
            cleaned_row = [str(item).replace('\n', '') for item in row]  # Remove newline characters
            data = '|'.join(cleaned_row)

            file.write(data + '\n')
            print(f"@Position :: {_}")

        # Write number of records at the end of the file
        file.write(f'{num_records}')

    print(f'Fake dataset with fileName {file_name} and {num_records} records generated successfully.')


if __name__ == '__main__':
    generate_data()
