from faker import Faker
import os, random

# Initialize Faker to generate fake data
fake = Faker()

file_dir = os.getenv('FILE_OUTPUT_DIRECTORY', '/Users/srirajkadimisetty/sample-data/txt/')
file_name = os.getenv('FILE_NAME', fake.lexify(text='customer_????'))

# Define the number of records to generate
num_records = os.getenv('NUMBER_OF_FAKE_RECORDS', 10)

output_file = f"{file_dir}{file_name}_{str(num_records)}.txt"


def generate_data():
    # Define the number of columns in the dataset (minimum 13 columns)
    num_columns = 13

    # Define the header for the CSV file
    header = 'id,name,phone,address,dob,ssn,occupation,income,risk_score,transaction_amount,country,email,status'

    with open(output_file, mode='w') as file:
        # Write header row with commas
        file.write(header + '\n')

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

    print(f'Fake dataset with {num_records} records generated successfully.')


if __name__ == '__main__':
    generate_data()
