package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Menu implements AutoCloseable {//implements AutoCloseable
    private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("RestJPA");
    ;
    private static EntityManager entityManager = entityManagerFactory.createEntityManager();
    ;
    private static CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    private Dish dish;
    private static Discount discount;


    public static void createDish(Scanner scanner) {

        while (true) {
            System.out.println("Input name of dish:");
            String name = scanner.nextLine();
            System.out.println("Input price:");
            Double price = Double.parseDouble(scanner.nextLine());
            System.out.println("Input weight:");
            Double weight = Double.parseDouble(scanner.nextLine());
            System.out.println("Does the client have a discount?  Y/N:");
            String choice = scanner.nextLine();

            if (choice.equals("Y") || choice.equals("y")) {
                discount = Discount.VIP;
            } else if (choice.equals("N") || choice.equals("n")) {
                discount = Discount.REGULAR;
            }

            try {
                Dish dish = new Dish(name, price, weight, discount);
//                System.out.println(dish);
                entityManager.getTransaction().begin();
                entityManager.persist(dish);
                entityManager.getTransaction().commit();
                System.out.println("Dish was added successfully");
            } catch (Exception e) {
                entityManager.getTransaction().rollback();
                System.out.println("Dish was NOT added");
            }
            break;
        }
    }

    public static List<Dish> chooseByPrice(Scanner scanner) {
        List<Dish> dishList;
        System.out.println("Enter min price");
        int min = Integer.parseInt(scanner.next());
        System.out.println("Enter max price");
        int max = Integer.parseInt(scanner.next());

        CriteriaQuery<Dish> criteriaQuery = cb.createQuery(Dish.class);
        Root<Dish> root = criteriaQuery.from(Dish.class);
        Predicate pricePredicate = cb.between(root.get("price"), min, max);
        criteriaQuery.where(pricePredicate);
        TypedQuery<Dish> typedQuery = entityManager.createQuery(criteriaQuery);
        dishList = typedQuery.getResultList();
        System.out.println(dishList);
        return dishList;
    }

    public static List<Dish> chooseByDiscount() {
        List<Dish> dishList;
        CriteriaQuery<Dish> criteriaQuery = cb.createQuery(Dish.class);
        Root<Dish> root = criteriaQuery.from(Dish.class);
        Predicate discountPredicate = cb.equal(root.get("discount"), Discount.VIP);
        criteriaQuery.where(discountPredicate);
        TypedQuery<Dish> typedQuery = entityManager.createQuery(criteriaQuery);
        dishList = typedQuery.getResultList();
        System.out.println(dishList);
        return dishList;
    }

    public static List<Dish> chooseByWeight(Scanner scanner) {
        List<Dish> dishList;
        List<Dish> dishListMax = new ArrayList<>();
        double maxWeight = 1000.0;
        double totalWeight = 0.0;

        while (true) {
            System.out.println("Enter dish ID(only 1 item per ID), or type q to Exit");
            String id = scanner.nextLine().trim();
            if (id.equals("q")) {
                break;
            }
            CriteriaQuery<Dish> criteriaQuery = cb.createQuery(Dish.class);
            Root<Dish> root = criteriaQuery.from(Dish.class);
            Predicate idPredicate = cb.equal(root.get("id"), id);
            criteriaQuery.where(idPredicate);
            TypedQuery<Dish> typedQuery = entityManager.createQuery(criteriaQuery);
            dishList = typedQuery.getResultList();
            for (Dish item : dishList) {
                if (item.getWeight() + totalWeight < maxWeight) {
                    dishListMax.add(item);
                    totalWeight += item.getWeight();
                }
            }

        }
        for (Dish item : dishListMax) {
            System.out.println(item);
        }
        System.out.println(totalWeight);
        return dishListMax;
    }

    @Override
    public void close() throws Exception {
        entityManager.close();
        entityManagerFactory.close();
    }
}
