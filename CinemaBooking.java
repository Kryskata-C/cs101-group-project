import java.util.Scanner;

class Movie {
    private String title;
    private String genre;
    private int minutes;

    public Movie(String title, String genre, int minutes) {
        this.title = title;
        this.genre = genre;
        this.minutes = minutes;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public int getMinutes() {
        return minutes;
    }

    public String toString() {
        return title + " (" + genre + ", " + minutes + " min)";
    }
}

class Seat {
    private char row;
    private int number;
    private boolean booked;

    public Seat(char row, int number) {
        this.row = row;
        this.number = number;
        this.booked = false;
    }

    public char getRow() {
        return row;
    }

    public int getNumber() {
        return number;
    }

    public boolean isBooked() {
        return booked;
    }

    public String getCode() {
        return "" + row + number;
    }

    public int getPrice() {
        return 180;
    }

    public char getSymbol() {
        if (booked) {
            return 'X';
        }
        return 'O';
    }

    public void book() {
        booked = true;
    }

    public void cancel() {
        booked = false;
    }
}

class VipSeat extends Seat {
    public VipSeat(char row, int number) {
        super(row, number);
    }

    @Override
    public int getPrice() {
        return 300;
    }

    @Override
    public char getSymbol() {
        if (isBooked()) {
            return 'X';
        }
        return 'V';
    }
}

class Showtime {
    private Movie movie;
    private String time;
    private Seat[][] seats;

    public Showtime(Movie movie, String time, int rows, int seatsPerRow) {
        this.movie = movie;
        this.time = time;
        this.seats = new Seat[rows][seatsPerRow];
        for (int r = 0; r < rows; r++) {
            char rowLetter = (char) ('A' + r);
            for (int c = 0; c < seatsPerRow; c++) {
                if (r == rows - 1) {
                    seats[r][c] = new VipSeat(rowLetter, c + 1);
                } else {
                    seats[r][c] = new Seat(rowLetter, c + 1);
                }
            }
        }
    }

    public Movie getMovie() {
        return movie;
    }

    public String getTime() {
        return time;
    }

    public int countFreeSeats() {
        int free = 0;
        for (int r = 0; r < seats.length; r++) {
            for (int c = 0; c < seats[r].length; c++) {
                if (!seats[r][c].isBooked()) {
                    free++;
                }
            }
        }
        return free;
    }

    public void showSeats() {
        System.out.println();
        System.out.println("   " + movie.getTitle() + " at " + time);
        System.out.println();
        System.out.println("     ======== SCREEN ========");
        System.out.println();
        System.out.print("    ");
        for (int c = 0; c < seats[0].length; c++) {
            System.out.printf("%3d", c + 1);
        }
        System.out.println();
        for (int r = 0; r < seats.length; r++) {
            System.out.print("  " + seats[r][0].getRow() + " ");
            for (int c = 0; c < seats[r].length; c++) {
                System.out.print("  " + seats[r][c].getSymbol());
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("  O = free (180 baht)   V = VIP (300 baht)   X = taken");
    }

    public Seat findSeat(String code) {
        if (code.length() < 2 || code.length() > 3) {
            return null;
        }
        int r = Character.toUpperCase(code.charAt(0)) - 'A';
        int number = 0;
        for (int i = 1; i < code.length(); i++) {
            char ch = code.charAt(i);
            if (!Character.isDigit(ch)) {
                return null;
            }
            number = number * 10 + (ch - '0');
        }
        if (r < 0 || r >= seats.length || number < 1 || number > seats[r].length) {
            return null;
        }
        return seats[r][number - 1];
    }

    public String toString() {
        return time + "  " + movie + "  - " + countFreeSeats() + " seats free";
    }
}

class Ticket {
    private Showtime showtime;
    private Seat seat;

    public Ticket(Showtime showtime, Seat seat) {
        this.showtime = showtime;
        this.seat = seat;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public Seat getSeat() {
        return seat;
    }

    public String toString() {
        return showtime.getMovie().getTitle() + " | " + showtime.getTime()
                + " | Seat " + seat.getCode() + " | " + seat.getPrice() + " baht";
    }
}

class Customer {
    private String name;
    private int money;
    private Ticket[] tickets;
    private int ticketCount;

    public Customer(String name, int money) {
        this.name = name;
        this.money = money;
        this.tickets = new Ticket[20];
        this.ticketCount = 0;
    }

    public String getName() {
        return name;
    }

    public int getMoney() {
        return money;
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public boolean canAfford(int price) {
        return money >= price;
    }

    public boolean hasRoomForTicket() {
        return ticketCount < tickets.length;
    }

    public void addTicket(Ticket ticket) {
        tickets[ticketCount] = ticket;
        ticketCount++;
        money = money - ticket.getSeat().getPrice();
    }

    public Ticket getTicket(int index) {
        if (index < 0 || index >= ticketCount) {
            return null;
        }
        return tickets[index];
    }

    public Ticket removeTicket(int index) {
        Ticket removed = tickets[index];
        for (int i = index; i < ticketCount - 1; i++) {
            tickets[i] = tickets[i + 1];
        }
        ticketCount--;
        tickets[ticketCount] = null;
        money = money + removed.getSeat().getPrice();
        return removed;
    }

    public void showTickets() {
        if (ticketCount == 0) {
            System.out.println("You have no tickets yet.");
            return;
        }
        for (int i = 0; i < ticketCount; i++) {
            System.out.println((i + 1) + ") " + tickets[i]);
        }
    }
}

class Cinema {
    private String name;
    private Showtime[] showtimes;

    public Cinema(String name) {
        this.name = name;

        Movie space = new Movie("Last Orbit", "Sci-Fi", 128);
        Movie comedy = new Movie("My Neighbour's Goat", "Comedy", 95);
        Movie horror = new Movie("The Quiet Floor", "Horror", 110);

        showtimes = new Showtime[4];
        showtimes[0] = new Showtime(space, "13:00", 5, 8);
        showtimes[1] = new Showtime(comedy, "15:30", 5, 8);
        showtimes[2] = new Showtime(horror, "19:00", 5, 8);
        showtimes[3] = new Showtime(space, "21:30", 5, 8);

        bookSeats(showtimes[0], new String[] {"C4", "C5", "D4", "E1"});
        bookSeats(showtimes[1], new String[] {"A1", "A2", "B7"});
        bookSeats(showtimes[2], new String[] {"C3", "C4", "C5", "C6", "E4", "E5"});
        bookSeats(showtimes[3], new String[] {"B2", "D8"});
    }

    private void bookSeats(Showtime showtime, String[] codes) {
        for (int i = 0; i < codes.length; i++) {
            showtime.findSeat(codes[i]).book();
        }
    }

    public String getName() {
        return name;
    }

    public int getShowtimeCount() {
        return showtimes.length;
    }

    public Showtime getShowtime(int number) {
        if (number < 1 || number > showtimes.length) {
            return null;
        }
        return showtimes[number - 1];
    }

    public void showShowtimes() {
        System.out.println();
        System.out.println("===== " + name + " - Today =====");
        for (int i = 0; i < showtimes.length; i++) {
            System.out.println((i + 1) + ") " + showtimes[i]);
        }
    }
}

public class CinemaBooking {
    private static Scanner scanner = new Scanner(System.in);
    private static Cinema cinema = new Cinema("Star Cinema");
    private static Customer customer;

    public static void main(String[] args) {
        System.out.println("===== Welcome to " + cinema.getName() + " =====");
        System.out.print("Your name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            name = "Guest";
        }
        customer = new Customer(name, 1000);
        System.out.println("Hello " + customer.getName() + "! You have " + customer.getMoney() + " baht.");

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("===== MENU =====  (" + customer.getMoney() + " baht)");
            System.out.println("1) View showtimes");
            System.out.println("2) Book seats");
            System.out.println("3) My tickets");
            System.out.println("4) Cancel a ticket");
            System.out.println("5) Quit");
            int choice = readNumber("Choose: ");

            if (choice == 1) {
                viewShowtimes();
            } else if (choice == 2) {
                bookSeats();
            } else if (choice == 3) {
                System.out.println();
                customer.showTickets();
            } else if (choice == 4) {
                cancelTicket();
            } else if (choice == 5) {
                running = false;
            } else {
                System.out.println("Please choose 1 to 5.");
            }
        }
        System.out.println("Goodbye " + customer.getName() + ", enjoy the movie!");
    }

    private static Showtime chooseShowtime() {
        cinema.showShowtimes();
        int number = readNumber("Pick a showtime (0 = back): ");
        if (number == 0) {
            return null;
        }
        Showtime showtime = cinema.getShowtime(number);
        if (showtime == null) {
            System.out.println("There is no showtime " + number + ".");
        }
        return showtime;
    }

    private static void viewShowtimes() {
        Showtime showtime = chooseShowtime();
        if (showtime != null) {
            showtime.showSeats();
        }
    }

    private static void bookSeats() {
        Showtime showtime = chooseShowtime();
        if (showtime == null) {
            return;
        }

        while (true) {
            showtime.showSeats();
            System.out.print("Seat to book, e.g. B4 (0 = done): ");
            String code = scanner.nextLine().trim();
            if (code.equals("0")) {
                return;
            }

            Seat seat = showtime.findSeat(code);
            if (seat == null) {
                System.out.println("There is no seat called " + code + ".");
            } else if (seat.isBooked()) {
                System.out.println("Seat " + seat.getCode() + " is already taken.");
            } else if (!customer.canAfford(seat.getPrice())) {
                System.out.println("Not enough money. You have " + customer.getMoney() + " baht.");
            } else if (!customer.hasRoomForTicket()) {
                System.out.println("You cannot hold any more tickets.");
            } else {
                seat.book();
                Ticket ticket = new Ticket(showtime, seat);
                customer.addTicket(ticket);
                System.out.println("Booked! " + ticket);
                System.out.println("Money left: " + customer.getMoney() + " baht");
            }
        }
    }

    private static void cancelTicket() {
        System.out.println();
        customer.showTickets();
        if (customer.getTicketCount() == 0) {
            return;
        }
        int number = readNumber("Ticket to cancel (0 = back): ");
        Ticket ticket = customer.getTicket(number - 1);
        if (ticket == null) {
            return;
        }
        ticket.getSeat().cancel();
        customer.removeTicket(number - 1);
        System.out.println("Cancelled " + ticket);
        System.out.println("Refunded " + ticket.getSeat().getPrice() + " baht. You have " + customer.getMoney() + " baht.");
    }

    private static int readNumber(String prompt) {
        System.out.print(prompt);
        String text = scanner.nextLine().trim();
        if (text.isEmpty() || text.length() > 5) {
            return -1;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return -1;
            }
        }
        return Integer.parseInt(text);
    }
}
