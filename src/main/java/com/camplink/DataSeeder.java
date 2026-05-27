package com.camplink;

import com.camplink.entity.*;
import com.camplink.repository.*;

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final ReviewRepository reviewRepo;
    private final ShoppingRequestRepository requestRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepo.count() > 0) {
            log.info("Database already seeded — skipping.");
            return;
        }

        log.info("Seeding database with sample data...");

        final String pw = passwordEncoder.encode("Test1234!");

        // ── Users ────────────────────────────────────────────────────────────────
        User admin = save(user("admin@camplink.com",   pw, "Admin User",     UserRole.ADMIN));
        User s1    = save(user("john@camplink.com",    pw, "John Banda",     UserRole.SELLER));
        User s2    = save(user("mary@camplink.com",    pw, "Mary Phiri",     UserRole.SELLER));
        User b1    = save(user("alice@camplink.com",   pw, "Alice Tembo",    UserRole.BUYER));
        User b2    = save(user("bob@camplink.com",     pw, "Bob Mwale",      UserRole.BUYER));

        // ── Products — John's store ───────────────────────────────────────────
        Product nshima   = save(product(s1, "Nshima & Relish",    "Food",        "Fresh nshima with chicken relish and vegetables. Ready at lunch.",        35.00));
        Product samosa   = save(product(s1, "Samosa Pack (5)",    "Food",        "Crispy beef samosas, freshly fried. Great for snacking between lectures.", 25.00));
        Product bread    = save(product(s1, "Sliced Bread Loaf",  "Groceries",   "Standard 700g white bread loaf.",                                         18.00));
        Product sugar    = save(product(s1, "Sugar 2kg",          "Groceries",   "Refined white sugar, 2kg packet.",                                        42.00));
        Product notebook = save(product(s1, "A4 Notebook 200pg",  "Stationery",  "Hard-cover A4 notebook, 200 pages, ruled.",                               28.00));
        Product charger  = save(product(s1, "USB-C Fast Charger", "Electronics", "65W GaN USB-C fast charger, compatible with most Android phones.",        180.00));

        // ── Products — Mary's store ──────────────────────────────────────────
        Product earphones = save(product(s2, "Wireless Earbuds",    "Electronics", "Bluetooth 5.3 earbuds with noise cancellation and 24h battery life.",   350.00));
        Product hoodie    = save(product(s2, "UNZA Hoodie (M)",     "Clothes",     "Official UNZA hoodie in grey, size Medium. 100% cotton.",               250.00));
        Product printing  = save(product(s2, "Typing & Printing",   "Services",    "Per-page typing and printing service. A4 black & white, same day.",       3.50));
        Product mathSet   = save(product(s2, "Math Set",            "Stationery",  "Complete geometry set: compass, protractor, ruler, set squares.",         55.00));
        Product soap      = save(product(s2, "Soap Pack (3 bars)",  "Groceries",   "Multipurpose bath soap, pack of 3 bars.",                                30.00));
        Product fruitSalad= save(product(s2, "Fruit Salad Cup",     "Food",        "Fresh seasonal fruit salad, 300ml cup. Prepared fresh each morning.",     20.00));

        // ── Orders ───────────────────────────────────────────────────────────
        // Delivered order (so buyer can leave review)
        OrderLine l1 = OrderLine.builder()
                .productId(nshima.getId()).productName(nshima.getName())
                .price(nshima.getPrice()).quantity(2).build();
        OrderLine l2 = OrderLine.builder()
                .productId(samosa.getId()).productName(samosa.getName())
                .price(samosa.getPrice()).quantity(1).build();

        Order delivered = Order.builder()
                .id(UUID.randomUUID().toString())
                .buyer(b1).seller(s1)
                .items(List.of(l1, l2))
                .total(nshima.getPrice().multiply(BigDecimal.TWO).add(samosa.getPrice()))
                .status(OrderStatus.DELIVERED)
                .deliveryMethod(DeliveryMethod.PICKUP)
                .deliveryLocation("Great East Road Campus gate")
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .paymentStatus(PaymentStatus.PAID)
                .build();
        orderRepo.save(delivered);

        // Pending order from bob
        OrderLine l3 = OrderLine.builder()
                .productId(charger.getId()).productName(charger.getName())
                .price(charger.getPrice()).quantity(1).build();

        Order pending = Order.builder()
                .id(UUID.randomUUID().toString())
                .buyer(b2).seller(s1)
                .items(List.of(l3))
                .total(charger.getPrice())
                .status(OrderStatus.PENDING)
                .deliveryMethod(DeliveryMethod.DELIVERY)
                .deliveryLocation("Sinozulu Hostel, Room 14")
                .paymentMethod(PaymentMethod.MTN_MOMO)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();
        orderRepo.save(pending);

        // Confirmed order from alice at Mary's store
        OrderLine l4 = OrderLine.builder()
                .productId(hoodie.getId()).productName(hoodie.getName())
                .price(hoodie.getPrice()).quantity(1).build();

        Order confirmed = Order.builder()
                .id(UUID.randomUUID().toString())
                .buyer(b1).seller(s2)
                .items(List.of(l4))
                .total(hoodie.getPrice())
                .status(OrderStatus.CONFIRMED)
                .deliveryMethod(DeliveryMethod.PICKUP)
                .deliveryLocation("Library entrance")
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();
        orderRepo.save(confirmed);

        // ── Reviews ──────────────────────────────────────────────────────────
        reviewRepo.save(Review.builder()
                .id(UUID.randomUUID().toString())
                .seller(s1).buyer(b1)
                .orderId(delivered.getId())
                .rating(5)
                .comment("Food was excellent and delivered on time. Highly recommend John's nshima!")
                .build());

        reviewRepo.save(Review.builder()
                .id(UUID.randomUUID().toString())
                .seller(s1).buyer(b2)
                .orderId(null)
                .rating(4)
                .comment("Good products, fair prices. Communication was quick.")
                .build());

        // ── Shopping Requests ─────────────────────────────────────────────────
        requestRepo.save(ShoppingRequest.builder()
                .id(UUID.randomUUID().toString())
                .requester(b1)
                .title("Groceries from Shoprite")
                .items(new ArrayList<>(List.of(
                        ShoppingRequestItem.builder().name("Milk 1L").quantity(2).estimatedPrice(BigDecimal.valueOf(28)).notes("Fresh, not long-life").build(),
                        ShoppingRequestItem.builder().name("Eggs (tray)").quantity(1).estimatedPrice(BigDecimal.valueOf(65)).notes(null).build(),
                        ShoppingRequestItem.builder().name("Bread (brown)").quantity(1).estimatedPrice(BigDecimal.valueOf(22)).notes("Albany if available").build()
                )))
                .deliveryHostel("Sinozulu")
                .deliveryRoom("Room 204")
                .budget(BigDecimal.valueOf(130))
                .note("Please get a receipt. I'll pay on delivery.")
                .runnerFee(BigDecimal.valueOf(20))
                .status(RequestStatus.OPEN)
                .build());

        requestRepo.save(ShoppingRequest.builder()
                .id(UUID.randomUUID().toString())
                .requester(b2)
                .title("Stationery from Sector A Shop")
                .items(new ArrayList<>(List.of(
                        ShoppingRequestItem.builder().name("A4 Notebook").quantity(2).estimatedPrice(BigDecimal.valueOf(28)).notes("Ruled, hard cover").build(),
                        ShoppingRequestItem.builder().name("Blue Biro Pens (pack)").quantity(1).estimatedPrice(BigDecimal.valueOf(15)).notes(null).build(),
                        ShoppingRequestItem.builder().name("Correction fluid").quantity(1).estimatedPrice(BigDecimal.valueOf(12)).notes(null).build()
                )))
                .deliveryHostel("Kafue")
                .deliveryRoom("Room 11B")
                .budget(BigDecimal.valueOf(85))
                .note("I have a 10am lecture so please deliver before 9:30am.")
                .runnerFee(BigDecimal.valueOf(15))
                .status(RequestStatus.OPEN)
                .build());

        // An already-accepted request for demo purposes
        ShoppingRequest accepted = ShoppingRequest.builder()
                .id(UUID.randomUUID().toString())
                .requester(b1)
                .title("Phone accessories")
                .items(new ArrayList<>(List.of(
                        ShoppingRequestItem.builder().name("USB-C cable 1m").quantity(1).estimatedPrice(BigDecimal.valueOf(45)).notes("Fast charging").build(),
                        ShoppingRequestItem.builder().name("Phone case").quantity(1).estimatedPrice(BigDecimal.valueOf(80)).notes("For Samsung A15, any colour").build()
                )))
                .deliveryHostel("Sinozulu")
                .deliveryRoom("Room 204")
                .budget(BigDecimal.valueOf(130))
                .note(null)
                .runnerFee(BigDecimal.valueOf(20))
                .status(RequestStatus.ACCEPTED)
                .runner(b2)
                .build();
        requestRepo.save(accepted);

        log.info("Seeding complete. Test accounts:");
        log.info("  admin@camplink.com  / Test1234!  (Admin)");
        log.info("  john@camplink.com   / Test1234!  (Seller)");
        log.info("  mary@camplink.com   / Test1234!  (Seller)");
        log.info("  alice@camplink.com  / Test1234!  (Buyer)");
        log.info("  bob@camplink.com    / Test1234!  (Buyer)");
    }

    private User user(String email, String pw, String name, UserRole role) {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .passwordHash(pw)
                .fullName(name)
                .role(role)
                .suspended(false)
                .build();
    }

    private Product product(User seller, String name, String category, String desc, double price) {
        return Product.builder()
                .id(UUID.randomUUID().toString())
                .seller(seller)
                .name(name)
                .category(category)
                .description(desc)
                .price(BigDecimal.valueOf(price))
                .available(true)
                .build();
    }

    private User save(User u)       { return userRepo.save(u); }
    private Product save(Product p) { return productRepo.save(p); }
}
