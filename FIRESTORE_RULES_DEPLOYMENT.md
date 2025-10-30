# Firebase Firestore Rules Deployment Guide

## Issue
The address functionality is failing with "PERMISSION_DENIED: Missing or insufficient permissions" because the Firestore security rules don't include the `addresses` collection.

## Solution
Deploy the updated `firestore.rules` file to your Firebase project.

## Steps to Deploy Firestore Rules

### Method 1: Firebase Console (Recommended for Quick Fix)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Click on **Firestore Database** in the left sidebar
4. Click on the **Rules** tab at the top
5. Copy the contents from `firestore.rules` file in your project root
6. Paste into the rules editor
7. Click **Publish** button

### Method 2: Firebase CLI (Recommended for Production)

1. Install Firebase CLI if not already installed:
   ```bash
   npm install -g firebase-tools
   ```

2. Login to Firebase:
   ```bash
   firebase login
   ```

3. Initialize Firebase in your project (if not already done):
   ```bash
   firebase init firestore
   ```
   - Select your Firebase project
   - Accept the default `firestore.rules` file
   - Accept the default `firestore.indexes.json` file

4. Deploy the rules:
   ```bash
   firebase deploy --only firestore:rules
   ```

## What the Rules Do

The updated rules include secure access for:

✅ **Addresses Collection**
- Users can only read/write their own addresses
- Ensures `userId` matches the authenticated user's UID

✅ **Users Collection**
- Users can only access their own user data

✅ **Orders Collection**
- Users can only access their own orders

✅ **Cart & Wishlist**
- Users can only access their own cart and wishlist

✅ **Products & Categories**
- Public read access (anyone can view)
- Only authenticated users can write

## Verify Rules are Working

After deploying:

1. Open the app
2. Go to Profile → Address Book
3. Click the "+" button to add an address
4. Fill in the address details and save
5. Check that the address appears in the Address Book
6. Go back to Home screen
7. Verify the address shows below "Hi, User!"

## Security Notes

- These rules are production-ready and secure
- Users can only access their own data
- All operations require authentication
- Data is properly isolated by userId

## Troubleshooting

If you still see permission errors:
1. Make sure you deployed the rules successfully
2. Check that the user is logged in (Firebase Auth)
3. Verify the `userId` field is correctly set in address documents
4. Check Firebase Console → Firestore → Rules tab to see active rules

